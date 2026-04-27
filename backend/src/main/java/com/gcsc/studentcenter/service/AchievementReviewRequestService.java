package com.gcsc.studentcenter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gcsc.studentcenter.dto.*;
import com.gcsc.studentcenter.entity.AchievementReviewRequest;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.UserRole;
import com.gcsc.studentcenter.repository.AchievementReviewRequestRepository;
import com.gcsc.studentcenter.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AchievementReviewRequestService {

    private final AchievementReviewRequestRepository achievementReviewRequestRepository;
    private final AppUserRepository appUserRepository;
    private final AchievementService achievementService;
    private final ReviewSettingsService reviewSettingsService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public AchievementReviewRequestService(
        AchievementReviewRequestRepository achievementReviewRequestRepository,
        AppUserRepository appUserRepository,
        AchievementService achievementService,
        ReviewSettingsService reviewSettingsService,
        UserService userService,
        ObjectMapper objectMapper
    ) {
        this.achievementReviewRequestRepository = achievementReviewRequestRepository;
        this.appUserRepository = appUserRepository;
        this.achievementService = achievementService;
        this.reviewSettingsService = reviewSettingsService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<AchievementReviewRequestResponse> listVisibleRequests(String username) {
        AppUser user = loadUser(username);
        List<AchievementReviewRequest> requests = isReviewer(user)
            ? achievementReviewRequestRepository.findAllByOrderByUpdatedAtDesc()
            : achievementReviewRequestRepository.findAllByRequester_UsernameOrderByUpdatedAtDesc(username);
        return requests.stream()
            .filter(r -> {
                if ("pending".equals(r.getStatus())) {
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
    public AchievementReviewRequestResponse submit(String username, AchievementReviewSubmitRequest request) {
        AppUser requester = loadUser(username);
        if (requester.getRole() != UserRole.STUDENT && requester.getRole() != UserRole.CADRE) {
            throw new IllegalArgumentException("仅学生可提交成就审核");
        }
        if (!reviewSettingsService.isAchievementReviewEnabled()) {
            throw new IllegalArgumentException("当前未开启成就审核");
        }

        String action = normalizeAction(request.getAction());
        String category = requireText(request.getCategory(), "成就分类不能为空");
        AchievementRecordRequest payload = request.getPayload();
        if (payload == null) {
            throw new IllegalArgumentException("提交内容不能为空");
        }
        if ("update".equals(action)) {
            if (request.getRecordId() == null) {
                throw new IllegalArgumentException("修改审核必须指定成就记录");
            }
            achievementService.getById(username, requester.getRole().name(), category, request.getRecordId());
        }
        validatePayload(payload);

        LocalDateTime now = LocalDateTime.now();
        AchievementReviewRequest entity = new AchievementReviewRequest();
        entity.setRequester(requester);
        entity.setReviewer(null);
        entity.setCategory(category);
        entity.setAction(action);
        entity.setStatus(reviewSettingsService.isAchievementReviewAutoApprove() ? "approved" : "pending");
        entity.setRecordId(request.getRecordId());
        entity.setTitle(resolveTitle(action, request.getTitle()));
        entity.setSummary(trimToNull(request.getSummary()));
        entity.setPayloadJson(writeJson(payload));
        entity.setPayloadSnapshotJson(writeJson(request.getPayloadSnapshot()));
        entity.setChangesJson(writeJson(request.getChanges()));
        entity.setRejectionReason("");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        AchievementReviewRequest saved = achievementReviewRequestRepository.save(entity);
        if (reviewSettingsService.isAchievementReviewAutoApprove()) {
            return toResponse(applyApprovedRequest(saved, null));
        }
        return toResponse(saved);
    }

    @Transactional
    public AchievementReviewRequestResponse approve(Long requestId, String reviewerUsername) {
        AppUser reviewer = loadReviewer(reviewerUsername);
        AchievementReviewRequest request = loadRequest(requestId);
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
    public AchievementReviewRequestResponse reject(Long requestId, String reviewerUsername, String reason) {
        AppUser reviewer = loadReviewer(reviewerUsername);
        AchievementReviewRequest request = loadRequest(requestId);
        if ("rejected".equals(request.getStatus())) {
            throw new IllegalArgumentException("该审核请求已被驳回");
        }
        if (!"pending".equals(request.getStatus())) {
            throw new IllegalArgumentException("该审核请求已处理");
        }
        ensureReviewerCanAccessRequest(reviewer, request);
        String safeReason = requireText(reason, "驳回时必须填写理由");

        request.setStatus("rejected");
        request.setReviewer(reviewer);
        request.setRejectionReason(safeReason);
        request.setUpdatedAt(LocalDateTime.now());
        return toResponse(achievementReviewRequestRepository.save(request));
    }

    @Transactional
    public void cancel(Long requestId, String username) {
        AchievementReviewRequest request = loadRequest(requestId);
        if (!"pending".equals(request.getStatus())) {
            throw new IllegalArgumentException("只能取消待审核的申请");
        }
        if (!request.getRequester().getUsername().equals(username)) {
            throw new IllegalArgumentException("只能取消自己的申请");
        }
        achievementReviewRequestRepository.delete(request);
    }

    @Transactional
    public AchievementReviewRequestResponse setSupportingDocuments(Long requestId, String username, List<Map<String, String>> documents) {
        AchievementReviewRequest request = loadRequest(requestId);
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
        return toResponse(achievementReviewRequestRepository.save(request));
    }

    private AchievementReviewRequestResponse toResponse(AchievementReviewRequest request) {
        return new AchievementReviewRequestResponse(
            request.getId(),
            "achievement",
            request.getAction(),
            request.getCategory(),
            resolveCategoryLabel(request.getCategory()),
            request.getRecordId(),
            request.getTitle(),
            request.getSummary(),
            request.getStatus(),
            toUserResponse(request.getRequester()),
            request.getReviewer() == null ? null : toUserResponse(request.getReviewer()),
            List.of("TEACHER", "ADMIN"),
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
            throw new IllegalArgumentException("无权限处理成就审核");
        }
        return reviewer;
    }

    private boolean isReviewer(AppUser user) {
        return user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.TEACHER || user.getRole() == UserRole.CADRE;
    }

    private AchievementReviewRequest loadRequest(Long requestId) {
        return achievementReviewRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("审核请求不存在"));
    }

    private void ensurePending(AchievementReviewRequest request) {
        if (!"pending".equals(request.getStatus())) {
            throw new IllegalArgumentException("该审核请求已处理");
        }
    }

    private void ensureReviewerCanAccessRequest(AppUser reviewer, AchievementReviewRequest request) {
        // ADMIN can handle any request
        if (reviewer.getRole() == UserRole.ADMIN) {
            return;
        }
        // TEACHER can only handle requests from their assigned class students
        if (reviewer.getRole() == UserRole.TEACHER) {
            if (userService.isStudentInTeacherAssignedClass(reviewer, request.getRequester())) {
                return;
            }
            throw new IllegalArgumentException("无权处理该审核请求");
        }
        // CADRE can only handle requests from students in their own class
        if (reviewer.getRole() == UserRole.CADRE) {
            if (isStudentInCadreOwnClass(reviewer, request.getRequester())) {
                return;
            }
            throw new IllegalArgumentException("无权处理该审核请求");
        }
    }

    private AchievementReviewRequest applyApprovedRequest(AchievementReviewRequest request, AppUser reviewer) {
        AchievementRecordRequest payload = readPayload(request.getPayloadJson());
        AchievementRecordResponse applied = "create".equals(request.getAction())
            ? achievementService.create(request.getRequester().getUsername(), request.getCategory(), payload)
            : achievementService.update(
                request.getRequester().getUsername(),
                request.getRequester().getRole().name(),
                request.getCategory(),
                request.getRecordId(),
                payload
            );

        request.setRecordId(applied.getId());
        request.setStatus("approved");
        request.setReviewer(reviewer);
        request.setRejectionReason("");
        request.setUpdatedAt(LocalDateTime.now());
        return achievementReviewRequestRepository.save(request);
    }

    private void validatePayload(AchievementRecordRequest payload) {
        achievementService.validateReviewPayload(payload);
    }

    private String normalizeAction(String action) {
        String normalized = requireText(action, "审核动作不能为空").toLowerCase(Locale.ROOT);
        if (!"create".equals(normalized) && !"update".equals(normalized)) {
            throw new IllegalArgumentException("审核动作无效");
        }
        return normalized;
    }

    private String resolveTitle(String action, String title) {
        String safeTitle = trimToNull(title);
        if (safeTitle != null) {
            return safeTitle;
        }
        return "update".equals(action) ? "成就修改待审核" : "成就新增待审核";
    }

    private String resolveCategoryLabel(String category) {
        return switch (category) {
            case "contest" -> "学科竞赛、文体艺术";
            case "paper" -> "发表学术论文";
            case "journal" -> "发表期刊作品";
            case "patent" -> "专利(著作权)授权数(项)";
            case "certificate" -> "职业资格证书";
            case "research" -> "学生参与教师科研项目情况";
            case "works" -> "创作、表演的代表性作品";
            case "doubleHundred" -> "双百工程";
            case "ieerTraining" -> "大学生创新创业训练计划项目";
            default -> category;
        };
    }

    private String requireText(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new IllegalArgumentException(message);
        }
        return trimmed;
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

    private AchievementRecordRequest readPayload(String json) {
        try {
            return objectMapper.readValue(json, AchievementRecordRequest.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("审核内容解析失败");
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
