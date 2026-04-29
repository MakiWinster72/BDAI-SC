package com.gcsc.studentcenter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gcsc.studentcenter.dto.ProfileReviewDecisionRequest;
import com.gcsc.studentcenter.dto.ProfileReviewRequestResponse;
import com.gcsc.studentcenter.dto.ProfileReviewSubmitRequest;
import com.gcsc.studentcenter.dto.ReviewUserResponse;
import com.gcsc.studentcenter.dto.StudentProfileRequest;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.ProfileReviewRequest;
import com.gcsc.studentcenter.entity.UserRole;
import com.gcsc.studentcenter.repository.AppUserRepository;
import com.gcsc.studentcenter.repository.ProfileReviewRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ProfileReviewRequestService {

    private final ProfileReviewRequestRepository profileReviewRequestRepository;
    private final AppUserRepository appUserRepository;
    private final StudentProfileService studentProfileService;
    private final ReviewSettingsService reviewSettingsService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public ProfileReviewRequestService(
        ProfileReviewRequestRepository profileReviewRequestRepository,
        AppUserRepository appUserRepository,
        StudentProfileService studentProfileService,
        ReviewSettingsService reviewSettingsService,
        UserService userService,
        ObjectMapper objectMapper
    ) {
        this.profileReviewRequestRepository = profileReviewRequestRepository;
        this.appUserRepository = appUserRepository;
        this.studentProfileService = studentProfileService;
        this.reviewSettingsService = reviewSettingsService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<ProfileReviewRequestResponse> listVisibleRequests(String username) {
        AppUser user = loadUser(username);
        List<ProfileReviewRequest> requests = isReviewer(user)
            ? profileReviewRequestRepository.findAllByOrderByUpdatedAtDesc()
            : profileReviewRequestRepository.findAllByRequester_UsernameOrderByUpdatedAtDesc(username);
        return requests.stream()
            .filter(r -> {
                if ("pending".equals(r.getStatus())) {
                    // pending: requester can always see their own pending request
                    if (r.getRequester().getUsername().equals(username)) return true;
                    // pending: only reviewers (TEACHER/ADMIN/CADRE) can see, not regular students
                    if (!isReviewer(user)) return false;
                    // ADMIN can see all pending
                    if (user.getRole() == UserRole.ADMIN) {
                        return true;
                    }
                    // TEACHER can only see their assigned class students
                    if (user.getRole() == UserRole.TEACHER) {
                        return userService.isStudentInTeacherAssignedClass(user, r.getRequester());
                    }
                    // CADRE can only see their own class students
                    if (user.getRole() == UserRole.CADRE) {
                        return isStudentInCadreOwnClass(user, r.getRequester());
                    }
                    return true;
                }
                // processed: only requester or reviewer can see
                return r.getRequester().getUsername().equals(username)
                    || (r.getReviewer() != null && r.getReviewer().getUsername().equals(username));
            })
            .map(this::toResponse)
            .toList();
    }

    private boolean isStudentInCadreOwnClass(AppUser cadre, AppUser student) {
        String cadreClass = cadre.getClassName();
        if (cadreClass == null || cadreClass.isBlank()) {
            return false; // CADRE with no class set sees nothing
        }
        String studentClass = student.getClassName();
        if (studentClass == null || studentClass.isBlank()) {
            return false;
        }
        return cadreClass.trim().equals(studentClass.trim());
    }

    @Transactional
    public ProfileReviewRequestResponse submit(String username, ProfileReviewSubmitRequest request) {
        AppUser requester = loadUser(username);
        if (requester.getRole() != UserRole.STUDENT && requester.getRole() != UserRole.CADRE) {
            throw new IllegalArgumentException("仅学生可提交个人信息审核");
        }
        if (!reviewSettingsService.isProfileReviewEnabled()) {
            throw new IllegalArgumentException("当前未开启个人信息审核");
        }

        LocalDateTime now = LocalDateTime.now();
        ProfileReviewRequest entity = new ProfileReviewRequest();
        entity.setRequester(requester);
        entity.setReviewer(null);
        entity.setStatus(reviewSettingsService.isProfileReviewAutoApprove() ? "approved" : "pending");
        entity.setTitle(trimToNull(request.getTitle()) != null ? request.getTitle() : "个人信息修改待审核");
        entity.setSummary(trimToNull(request.getSummary()));
        entity.setPayloadSnapshotJson(writeJson(request.getPayloadSnapshot()));
        entity.setChangesJson(writeJson(request.getChanges()));
        entity.setRejectionReason("");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        ProfileReviewRequest saved = profileReviewRequestRepository.save(entity);

        if (reviewSettingsService.isProfileReviewAutoApprove()) {
            return toResponse(applyApprovedRequest(saved, null));
        }
        return toResponse(saved);
    }

    @Transactional
    public ProfileReviewRequestResponse approve(Long requestId, String reviewerUsername) {
        AppUser reviewer = loadReviewer(reviewerUsername);
        ProfileReviewRequest request = loadRequest(requestId);
        if ("approved".equals(request.getStatus())) {
            throw new IllegalArgumentException("该审核请求已被其他人处理");
        }
        if (!"pending".equals(request.getStatus())) {
            throw new IllegalArgumentException("该审核请求已处理");
        }
        ensureReviewerCanAccessRequest(reviewer, request);
        return toResponse(applyApprovedRequest(request, reviewer));
    }

    @Transactional
    public ProfileReviewRequestResponse reject(Long requestId, String reviewerUsername, ProfileReviewDecisionRequest decisionRequest) {
        AppUser reviewer = loadReviewer(reviewerUsername);
        ProfileReviewRequest request = loadRequest(requestId);
        if ("rejected".equals(request.getStatus())) {
            throw new IllegalArgumentException("该审核请求已被驳回");
        }
        if (!"pending".equals(request.getStatus())) {
            throw new IllegalArgumentException("该审核请求已处理");
        }
        ensureReviewerCanAccessRequest(reviewer, request);
        String reason = trimToNull(decisionRequest.getReason());
        if (reason == null || reason.isEmpty()) {
            throw new IllegalArgumentException("驳回时必须填写理由");
        }

        request.setStatus("rejected");
        request.setReviewer(reviewer);
        request.setRejectionReason(reason);
        request.setUpdatedAt(LocalDateTime.now());
        return toResponse(profileReviewRequestRepository.save(request));
    }

    @Transactional
    public void cancel(Long requestId, String username) {
        ProfileReviewRequest request = loadRequest(requestId);
        if (!"pending".equals(request.getStatus())) {
            throw new IllegalArgumentException("只能取消待审核的申请");
        }
        if (!request.getRequester().getUsername().equals(username)) {
            throw new IllegalArgumentException("只能取消自己的申请");
        }
        profileReviewRequestRepository.delete(request);
    }

    @Transactional
    public ProfileReviewRequestResponse setSupportingDocuments(Long requestId, String username, List<Map<String, String>> documents) {
        ProfileReviewRequest request = loadRequest(requestId);
        if (!"pending".equals(request.getStatus())) {
            throw new IllegalArgumentException("只有待审核状态才能上传证明资料");
        }
        if (!request.getRequester().getUsername().equals(username)) {
            throw new IllegalArgumentException("只有申请人才能上传证明资料");
        }
        if (documents != null) {
            for (Map<String, String> doc : documents) {
                if ("text".equals(doc.get("type"))) {
                    if (doc.get("content") == null || doc.get("content").isBlank()) {
                        throw new IllegalArgumentException("证明资料文本内容不能为空");
                    }
                } else {
                    if (doc.get("url") == null || doc.get("url").isBlank()) {
                        throw new IllegalArgumentException("证明资料链接不能为空");
                    }
                }
            }
        }
        String json = (documents == null || documents.isEmpty()) ? null : writeJson(documents);
        request.setSupportingDocumentsJson(json);
        request.setUpdatedAt(LocalDateTime.now());
        return toResponse(profileReviewRequestRepository.save(request));
    }

    private ProfileReviewRequestResponse toResponse(ProfileReviewRequest request) {
        return new ProfileReviewRequestResponse(
            request.getId(),
            "profile",
            request.getStatus(),
            toUserResponse(request.getRequester()),
            request.getReviewer() == null ? null : toUserResponse(request.getReviewer()),
            List.of("TEACHER", "ADMIN"),
            request.getTitle(),
            request.getSummary(),
            nullToEmpty(request.getRejectionReason()),
            readJsonNode(request.getPayloadSnapshotJson()),
            readChanges(request.getChangesJson()),
            readSupportingDocuments(request.getSupportingDocumentsJson()),
            request.getCreatedAt(),
            request.getUpdatedAt()
        );
    }

    private ReviewUserResponse toUserResponse(AppUser user) {
        return new ReviewUserResponse(
            user.getUsername(),
            user.getDisplayName(),
            user.getRole() == null ? "" : user.getRole().name(),
            user.getStudentNo(),
            user.getClassName()
        );
    }

    private AppUser loadUser(String username) {
        return appUserRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    private AppUser loadReviewer(String username) {
        AppUser reviewer = loadUser(username);
        if (!isReviewer(reviewer)) {
            throw new IllegalArgumentException("无权限处理个人信息审核");
        }
        return reviewer;
    }

    private boolean isReviewer(AppUser user) {
        return user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.TEACHER || user.getRole() == UserRole.CADRE;
    }

    private ProfileReviewRequest loadRequest(Long requestId) {
        return profileReviewRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("审核请求不存在"));
    }

    private void ensurePending(ProfileReviewRequest request) {
        if (!"pending".equals(request.getStatus())) {
            throw new IllegalArgumentException("该审核请求已处理");
        }
    }

    private void ensureReviewerCanAccessRequest(AppUser reviewer, ProfileReviewRequest request) {
        if (reviewer.getRole() == UserRole.ADMIN) {
            return;
        }
        if (reviewer.getRole() == UserRole.TEACHER) {
            if (userService.isStudentInTeacherAssignedClass(reviewer, request.getRequester())) {
                return;
            }
            throw new IllegalArgumentException("无权处理该审核请求");
        }
        if (reviewer.getRole() == UserRole.CADRE) {
            if (isStudentInCadreOwnClass(reviewer, request.getRequester())) {
                return;
            }
            throw new IllegalArgumentException("无权处理该审核请求");
        }
    }

    private ProfileReviewRequest applyApprovedRequest(ProfileReviewRequest request, AppUser reviewer) {
        StudentProfileRequest profileRequest = readProfileRequest(request.getPayloadSnapshotJson());

        studentProfileService.saveProfile(request.getRequester().getUsername(), profileRequest);

        request.setStatus("approved");
        request.setReviewer(reviewer);
        request.setRejectionReason("");
        request.setUpdatedAt(LocalDateTime.now());
        return profileReviewRequestRepository.save(request);
    }

    private StudentProfileRequest readProfileRequest(String json) {
        try {
            return objectMapper.readValue(json, StudentProfileRequest.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("个人信息数据解析失败");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String writeJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("审核数据保存失败");
        }
    }

    private JsonNode readJsonNode(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException exception) {
            return null;
        }
    }

    private List<Map<String, Object>> readChanges(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (JsonProcessingException exception) {
            return List.of();
        }
    }

    private List<Map<String, Object>> readSupportingDocuments(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (JsonProcessingException exception) {
            return List.of();
        }
    }
}