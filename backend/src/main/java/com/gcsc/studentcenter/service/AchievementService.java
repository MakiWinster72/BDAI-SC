package com.gcsc.studentcenter.service;

import com.gcsc.studentcenter.dto.AchievementRecordRequest;
import com.gcsc.studentcenter.dto.AchievementRecordResponse;
import com.gcsc.studentcenter.entity.*;
import com.gcsc.studentcenter.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AchievementService {

    private final AppUserRepository appUserRepository;
    private final AchievementContestRepository achievementContestRepository;
    private final AchievementPaperRepository achievementPaperRepository;
    private final AchievementJournalRepository achievementJournalRepository;
    private final AchievementPatentRepository achievementPatentRepository;
    private final AchievementCertificateRepository achievementCertificateRepository;
    private final AchievementResearchRepository achievementResearchRepository;
    private final AchievementWorksRepository achievementWorksRepository;
    private final AchievementDoubleHundredRepository achievementDoubleHundredRepository;
    private final AchievementIeerTrainingRepository achievementIeerTrainingRepository;
    private final AchievementUploadSettingsService achievementUploadSettingsService;

    public AchievementService(
        AppUserRepository appUserRepository,
        AchievementContestRepository achievementContestRepository,
        AchievementPaperRepository achievementPaperRepository,
        AchievementJournalRepository achievementJournalRepository,
        AchievementPatentRepository achievementPatentRepository,
        AchievementCertificateRepository achievementCertificateRepository,
        AchievementResearchRepository achievementResearchRepository,
        AchievementWorksRepository achievementWorksRepository,
        AchievementDoubleHundredRepository achievementDoubleHundredRepository,
        AchievementIeerTrainingRepository achievementIeerTrainingRepository,
        AchievementUploadSettingsService achievementUploadSettingsService
    ) {
        this.appUserRepository = appUserRepository;
        this.achievementContestRepository = achievementContestRepository;
        this.achievementPaperRepository = achievementPaperRepository;
        this.achievementJournalRepository = achievementJournalRepository;
        this.achievementPatentRepository = achievementPatentRepository;
        this.achievementCertificateRepository = achievementCertificateRepository;
        this.achievementResearchRepository = achievementResearchRepository;
        this.achievementWorksRepository = achievementWorksRepository;
        this.achievementDoubleHundredRepository = achievementDoubleHundredRepository;
        this.achievementIeerTrainingRepository = achievementIeerTrainingRepository;
        this.achievementUploadSettingsService = achievementUploadSettingsService;
    }

    public List<AchievementRecordResponse> list(
        String username,
        String category,
        String studentNo,
        String studentName
    ) {
        String safeStudentNo = normalizeStudentParam(studentNo);
        String safeStudentName = normalizeStudentParam(studentName);
        boolean hasStudentFilter = !safeStudentNo.isEmpty() || !safeStudentName.isEmpty();
        AppUser user = appUserRepository.findByUsername(username).orElse(null);
        boolean allowStudentFilter = hasStudentFilter && isPrivileged(user);
        if (allowStudentFilter) {
            String normalized = normalizeCategory(category);
            if (normalized == null) {
                return listAllByStudent(safeStudentNo, safeStudentName);
            }
            return listByStudent(safeStudentNo, safeStudentName, normalized);
        }
        if (category == null || category.isBlank()) {
            return listAll(username);
        }
        String normalized = normalizeCategory(category);
        if (normalized == null) {
            return listAll(username);
        }
        switch (normalized) {
            case "contest":
                return achievementContestRepository.findAllByAuthor_UsernameOrderByCreatedAtDesc(username)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            case "paper":
                return achievementPaperRepository.findAllByAuthor_UsernameOrderByCreatedAtDesc(username)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            case "journal":
                return achievementJournalRepository.findAllByAuthor_UsernameOrderByCreatedAtDesc(username)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            case "patent":
                return achievementPatentRepository.findAllByAuthor_UsernameOrderByCreatedAtDesc(username)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            case "certificate":
                return achievementCertificateRepository.findAllByAuthor_UsernameOrderByCreatedAtDesc(username)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            case "research":
                return achievementResearchRepository.findAllByAuthor_UsernameOrderByCreatedAtDesc(username)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            case "works":
                return achievementWorksRepository.findAllByAuthor_UsernameOrderByCreatedAtDesc(username)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            case "doubleHundred":
                return achievementDoubleHundredRepository.findAllByAuthor_UsernameOrderByCreatedAtDesc(username)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            case "ieerTraining":
                return achievementIeerTrainingRepository.findAllByAuthor_UsernameOrderByCreatedAtDesc(username)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            default:
                throw new IllegalArgumentException("无效的成就分类");
        }
    }

    public AchievementRecordResponse getById(String username, String role, String category, Long id) {
        switch (requireCategory(category)) {
            case "contest":
                return toResponse(loadContest(username, role, id));
            case "paper":
                return toResponse(loadPaper(username, role, id));
            case "journal":
                return toResponse(loadJournal(username, role, id));
            case "patent":
                return toResponse(loadPatent(username, role, id));
            case "certificate":
                return toResponse(loadCertificate(username, role, id));
            case "research":
                return toResponse(loadResearch(username, role, id));
            case "works":
                return toResponse(loadWorks(username, role, id));
            case "doubleHundred":
                return toResponse(loadDoubleHundred(username, role, id));
            case "ieerTraining":
                return toResponse(loadIeerTraining(username, role, id));
            default:
                throw new IllegalArgumentException("无效的成就分类");
        }
    }

    public AchievementRecordResponse create(String username, String category, AchievementRecordRequest request) {
        AppUser author = appUserRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Map<String, String> fields = safeFields(request.getFields());
        validateAchievementMedia(fields);
        switch (requireCategory(category)) {
            case "contest":
                return toResponse(saveContest(author, fields, request.getImageUrl()));
            case "paper":
                return toResponse(savePaper(author, fields, request.getImageUrl()));
            case "journal":
                return toResponse(saveJournal(author, fields, request.getImageUrl()));
            case "patent":
                return toResponse(savePatent(author, fields, request.getImageUrl()));
            case "certificate":
                return toResponse(saveCertificate(author, fields, request.getImageUrl()));
            case "research":
                return toResponse(saveResearch(author, fields, request.getImageUrl()));
            case "works":
                return toResponse(saveWorks(author, fields, request.getImageUrl()));
            case "doubleHundred":
                return toResponse(saveDoubleHundred(author, fields, request.getImageUrl()));
            case "ieerTraining":
                return toResponse(saveIeerTraining(author, fields, request.getImageUrl()));
            default:
                throw new IllegalArgumentException("无效的成就分类");
        }
    }

    public AchievementRecordResponse update(String username, String role, String category, Long id, AchievementRecordRequest request) {
        Map<String, String> fields = safeFields(request.getFields());
        validateAchievementMedia(fields);
        switch (requireCategory(category)) {
            case "contest":
                return toResponse(updateContest(username, role, id, fields, request.getImageUrl()));
            case "paper":
                return toResponse(updatePaper(username, role, id, fields, request.getImageUrl()));
            case "journal":
                return toResponse(updateJournal(username, role, id, fields, request.getImageUrl()));
            case "patent":
                return toResponse(updatePatent(username, role, id, fields, request.getImageUrl()));
            case "certificate":
                return toResponse(updateCertificate(username, role, id, fields, request.getImageUrl()));
            case "research":
                return toResponse(updateResearch(username, role, id, fields, request.getImageUrl()));
            case "works":
                return toResponse(updateWorks(username, role, id, fields, request.getImageUrl()));
            case "doubleHundred":
                return toResponse(updateDoubleHundred(username, role, id, fields, request.getImageUrl()));
            case "ieerTraining":
                return toResponse(updateIeerTraining(username, role, id, fields, request.getImageUrl()));
            default:
                throw new IllegalArgumentException("无效的成就分类");
        }
    }

    public void validateReviewPayload(AchievementRecordRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("提交内容不能为空");
        }
        Map<String, String> fields = safeFields(request.getFields());
        validateAchievementMedia(fields);
        String category = valueOf(fields, "category");
        if (!category.isEmpty()) {
            requireCategory(category);
        }
    }

    public void delete(String username, String role, String category, Long id) {
        switch (requireCategory(category)) {
            case "contest":
                achievementContestRepository.delete(loadContest(username, role, id));
                return;
            case "paper":
                achievementPaperRepository.delete(loadPaper(username, role, id));
                return;
            case "journal":
                achievementJournalRepository.delete(loadJournal(username, role, id));
                return;
            case "patent":
                achievementPatentRepository.delete(loadPatent(username, role, id));
                return;
            case "certificate":
                achievementCertificateRepository.delete(loadCertificate(username, role, id));
                return;
            case "research":
                achievementResearchRepository.delete(loadResearch(username, role, id));
                return;
            case "works":
                achievementWorksRepository.delete(loadWorks(username, role, id));
                return;
            case "doubleHundred":
                achievementDoubleHundredRepository.delete(loadDoubleHundred(username, role, id));
                return;
            case "ieerTraining":
                achievementIeerTrainingRepository.delete(loadIeerTraining(username, role, id));
                return;
            default:
                throw new IllegalArgumentException("无效的成就分类");
        }
    }

    private List<AchievementRecordResponse> listAll(String username) {
        List<AchievementRecordResponse> all = new ArrayList<>();
        all.addAll(achievementContestRepository.findAllByAuthor_UsernameOrderByCreatedAtDesc(username)
            .stream().map(this::toResponse).collect(Collectors.toList()));
        all.addAll(achievementPaperRepository.findAllByAuthor_UsernameOrderByCreatedAtDesc(username)
            .stream().map(this::toResponse).collect(Collectors.toList()));
        all.addAll(achievementJournalRepository.findAllByAuthor_UsernameOrderByCreatedAtDesc(username)
            .stream().map(this::toResponse).collect(Collectors.toList()));
        all.addAll(achievementPatentRepository.findAllByAuthor_UsernameOrderByCreatedAtDesc(username)
            .stream().map(this::toResponse).collect(Collectors.toList()));
        all.addAll(achievementCertificateRepository.findAllByAuthor_UsernameOrderByCreatedAtDesc(username)
            .stream().map(this::toResponse).collect(Collectors.toList()));
        all.addAll(achievementResearchRepository.findAllByAuthor_UsernameOrderByCreatedAtDesc(username)
            .stream().map(this::toResponse).collect(Collectors.toList()));
        all.addAll(achievementWorksRepository.findAllByAuthor_UsernameOrderByCreatedAtDesc(username)
            .stream().map(this::toResponse).collect(Collectors.toList()));
        all.addAll(achievementDoubleHundredRepository.findAllByAuthor_UsernameOrderByCreatedAtDesc(username)
            .stream().map(this::toResponse).collect(Collectors.toList()));
        all.addAll(achievementIeerTrainingRepository.findAllByAuthor_UsernameOrderByCreatedAtDesc(username)
            .stream().map(this::toResponse).collect(Collectors.toList()));
        return all.stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .collect(Collectors.toList());
    }

    private List<AchievementRecordResponse> listByStudent(
        String studentNo,
        String studentName,
        String category
    ) {
        if (category == null || category.isBlank()) {
            return listAllByStudent(studentNo, studentName);
        }
        String normalized = normalizeCategory(category);
        if (normalized == null) {
            return listAllByStudent(studentNo, studentName);
        }
        switch (normalized) {
            case "contest":
                return fetchContestByStudent(studentNo, studentName)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            case "paper":
                return fetchPaperByStudent(studentNo, studentName)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            case "journal":
                return fetchJournalByStudent(studentNo, studentName)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            case "patent":
                return fetchPatentByStudent(studentNo, studentName)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            case "certificate":
                return fetchCertificateByStudent(studentNo, studentName)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            case "research":
                return fetchResearchByStudent(studentNo, studentName)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            case "works":
                return fetchWorksByStudent(studentNo, studentName)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            case "doubleHundred":
                return fetchDoubleHundredByStudent(studentNo, studentName)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            case "ieerTraining":
                return fetchIeerTrainingByStudent(studentNo, studentName)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            default:
                throw new IllegalArgumentException("无效的成就分类");
        }
    }

    private List<AchievementRecordResponse> listAllByStudent(String studentNo, String studentName) {
        List<AchievementRecordResponse> all = new ArrayList<>();
        all.addAll(fetchContestByStudent(studentNo, studentName)
            .stream().map(this::toResponse).collect(Collectors.toList()));
        all.addAll(fetchPaperByStudent(studentNo, studentName)
            .stream().map(this::toResponse).collect(Collectors.toList()));
        all.addAll(fetchJournalByStudent(studentNo, studentName)
            .stream().map(this::toResponse).collect(Collectors.toList()));
        all.addAll(fetchPatentByStudent(studentNo, studentName)
            .stream().map(this::toResponse).collect(Collectors.toList()));
        all.addAll(fetchCertificateByStudent(studentNo, studentName)
            .stream().map(this::toResponse).collect(Collectors.toList()));
        all.addAll(fetchResearchByStudent(studentNo, studentName)
            .stream().map(this::toResponse).collect(Collectors.toList()));
        all.addAll(fetchWorksByStudent(studentNo, studentName)
            .stream().map(this::toResponse).collect(Collectors.toList()));
        all.addAll(fetchDoubleHundredByStudent(studentNo, studentName)
            .stream().map(this::toResponse).collect(Collectors.toList()));
        all.addAll(fetchIeerTrainingByStudent(studentNo, studentName)
            .stream().map(this::toResponse).collect(Collectors.toList()));
        return all.stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .collect(Collectors.toList());
    }

    private List<AchievementContest> fetchContestByStudent(String studentNo, String studentName) {
        if (!studentNo.isEmpty() && !studentName.isEmpty()) {
            return achievementContestRepository
                .findAllByStudentNoAndStudentNameOrderByCreatedAtDesc(studentNo, studentName);
        }
        if (!studentNo.isEmpty()) {
            return achievementContestRepository.findAllByStudentNoOrderByCreatedAtDesc(studentNo);
        }
        if (!studentName.isEmpty()) {
            return achievementContestRepository.findAllByStudentNameOrderByCreatedAtDesc(studentName);
        }
        return new ArrayList<>();
    }

    private List<AchievementPaper> fetchPaperByStudent(String studentNo, String studentName) {
        if (!studentNo.isEmpty() && !studentName.isEmpty()) {
            return achievementPaperRepository
                .findAllByStudentNoAndStudentNameOrderByCreatedAtDesc(studentNo, studentName);
        }
        if (!studentNo.isEmpty()) {
            return achievementPaperRepository.findAllByStudentNoOrderByCreatedAtDesc(studentNo);
        }
        if (!studentName.isEmpty()) {
            return achievementPaperRepository.findAllByStudentNameOrderByCreatedAtDesc(studentName);
        }
        return new ArrayList<>();
    }

    private List<AchievementJournal> fetchJournalByStudent(String studentNo, String studentName) {
        if (!studentNo.isEmpty() && !studentName.isEmpty()) {
            return achievementJournalRepository
                .findAllByStudentNoAndStudentNameOrderByCreatedAtDesc(studentNo, studentName);
        }
        if (!studentNo.isEmpty()) {
            return achievementJournalRepository.findAllByStudentNoOrderByCreatedAtDesc(studentNo);
        }
        if (!studentName.isEmpty()) {
            return achievementJournalRepository.findAllByStudentNameOrderByCreatedAtDesc(studentName);
        }
        return new ArrayList<>();
    }

    private List<AchievementPatent> fetchPatentByStudent(String studentNo, String studentName) {
        if (!studentNo.isEmpty() && !studentName.isEmpty()) {
            return achievementPatentRepository
                .findAllByStudentNoAndStudentNameOrderByCreatedAtDesc(studentNo, studentName);
        }
        if (!studentNo.isEmpty()) {
            return achievementPatentRepository.findAllByStudentNoOrderByCreatedAtDesc(studentNo);
        }
        if (!studentName.isEmpty()) {
            return achievementPatentRepository.findAllByStudentNameOrderByCreatedAtDesc(studentName);
        }
        return new ArrayList<>();
    }

    private List<AchievementCertificate> fetchCertificateByStudent(String studentNo, String studentName) {
        if (!studentNo.isEmpty() && !studentName.isEmpty()) {
            return achievementCertificateRepository
                .findAllByStudentNoAndStudentNameOrderByCreatedAtDesc(studentNo, studentName);
        }
        if (!studentNo.isEmpty()) {
            return achievementCertificateRepository.findAllByStudentNoOrderByCreatedAtDesc(studentNo);
        }
        if (!studentName.isEmpty()) {
            return achievementCertificateRepository.findAllByStudentNameOrderByCreatedAtDesc(studentName);
        }
        return new ArrayList<>();
    }

    private List<AchievementResearch> fetchResearchByStudent(String studentNo, String studentName) {
        if (!studentNo.isEmpty() && !studentName.isEmpty()) {
            return achievementResearchRepository
                .findAllByStudentNoAndStudentNameOrderByCreatedAtDesc(studentNo, studentName);
        }
        if (!studentNo.isEmpty()) {
            return achievementResearchRepository.findAllByStudentNoOrderByCreatedAtDesc(studentNo);
        }
        if (!studentName.isEmpty()) {
            return achievementResearchRepository.findAllByStudentNameOrderByCreatedAtDesc(studentName);
        }
        return new ArrayList<>();
    }

    private List<AchievementWorks> fetchWorksByStudent(String studentNo, String studentName) {
        if (!studentNo.isEmpty() && !studentName.isEmpty()) {
            return achievementWorksRepository
                .findAllByStudentNoAndStudentNameOrderByCreatedAtDesc(studentNo, studentName);
        }
        if (!studentNo.isEmpty()) {
            return achievementWorksRepository.findAllByStudentNoOrderByCreatedAtDesc(studentNo);
        }
        if (!studentName.isEmpty()) {
            return achievementWorksRepository.findAllByStudentNameOrderByCreatedAtDesc(studentName);
        }
        return new ArrayList<>();
    }

    private List<AchievementDoubleHundred> fetchDoubleHundredByStudent(String studentNo, String studentName) {
        if (!studentNo.isEmpty() && !studentName.isEmpty()) {
            return achievementDoubleHundredRepository
                .findAllByStudentNoAndStudentNameOrderByCreatedAtDesc(studentNo, studentName);
        }
        if (!studentNo.isEmpty()) {
            return achievementDoubleHundredRepository.findAllByStudentNoOrderByCreatedAtDesc(studentNo);
        }
        if (!studentName.isEmpty()) {
            return achievementDoubleHundredRepository.findAllByStudentNameOrderByCreatedAtDesc(studentName);
        }
        return new ArrayList<>();
    }

    private List<AchievementIeerTraining> fetchIeerTrainingByStudent(String studentNo, String studentName) {
        if (!studentNo.isEmpty() && !studentName.isEmpty()) {
            return achievementIeerTrainingRepository
                .findAllByStudentNoAndStudentNameOrderByCreatedAtDesc(studentNo, studentName);
        }
        if (!studentNo.isEmpty()) {
            return achievementIeerTrainingRepository.findAllByStudentNoOrderByCreatedAtDesc(studentNo);
        }
        if (!studentName.isEmpty()) {
            return achievementIeerTrainingRepository.findAllByStudentNameOrderByCreatedAtDesc(studentName);
        }
        return new ArrayList<>();
    }

    private boolean isPrivileged(AppUser user) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        return user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.TEACHER;
    }

    private String normalizeStudentParam(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }

    private AchievementContest loadContest(String username, String role, Long id) {
        AchievementContest contest = achievementContestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("成就不存在"));
        ensureOwner(username, role, contest.getAuthor());
        return contest;
    }

    private AchievementPaper loadPaper(String username, String role, Long id) {
        AchievementPaper paper = achievementPaperRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("成就不存在"));
        ensureOwner(username, role, paper.getAuthor());
        return paper;
    }

    private AchievementJournal loadJournal(String username, String role, Long id) {
        AchievementJournal journal = achievementJournalRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("成就不存在"));
        ensureOwner(username, role, journal.getAuthor());
        return journal;
    }

    private AchievementPatent loadPatent(String username, String role, Long id) {
        AchievementPatent patent = achievementPatentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("成就不存在"));
        ensureOwner(username, role, patent.getAuthor());
        return patent;
    }

    private AchievementCertificate loadCertificate(String username, String role, Long id) {
        AchievementCertificate certificate = achievementCertificateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("成就不存在"));
        ensureOwner(username, role, certificate.getAuthor());
        return certificate;
    }

    private AchievementResearch loadResearch(String username, String role, Long id) {
        AchievementResearch research = achievementResearchRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("成就不存在"));
        ensureOwner(username, role, research.getAuthor());
        return research;
    }

    private AchievementWorks loadWorks(String username, String role, Long id) {
        AchievementWorks works = achievementWorksRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("成就不存在"));
        ensureOwner(username, role, works.getAuthor());
        return works;
    }

    private AchievementDoubleHundred loadDoubleHundred(String username, String role, Long id) {
        AchievementDoubleHundred doubleHundred = achievementDoubleHundredRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("成就不存在"));
        ensureOwner(username, role, doubleHundred.getAuthor());
        return doubleHundred;
    }

    private AchievementIeerTraining loadIeerTraining(String username, String role, Long id) {
        AchievementIeerTraining ieerTraining = achievementIeerTrainingRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("成就不存在"));
        ensureOwner(username, role, ieerTraining.getAuthor());
        return ieerTraining;
    }

    private void ensureOwner(String username, String role, AppUser author) {
        if ("ADMIN".equals(role)) {
            return;
        }
        if (!author.getUsername().equals(username)) {
            throw new IllegalArgumentException("无权限操作该成就");
        }
    }

    private String requireCategory(String category) {
        String normalized = normalizeCategory(category);
        if (normalized == null) {
            throw new IllegalArgumentException("成就分类不能为空");
        }
        return normalized;
    }

    private String normalizeCategory(String category) {
        if (category == null) {
            return null;
        }
        String trimmed = category.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Map<String, String> safeFields(Map<String, String> fields) {
        return fields == null ? new HashMap<>() : fields;
    }

    private void validateAchievementMedia(Map<String, String> fields) {
        achievementUploadSettingsService.validateAchievementMedia(
            fields.get("_imageUrls"),
            fields.get("_attachments")
        );
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private AchievementContest saveContest(AppUser author, Map<String, String> fields, String imageUrl) {
        String contestName = valueOf(fields, "contestName");
        if (contestName.isEmpty()) {
            throw new IllegalArgumentException("竞赛名称不能为空");
        }
        AchievementContest contest = new AchievementContest();
        contest.setAuthor(author);
        contest.setStudentNo(valueOf(fields, "studentNo"));
        contest.setStudentName(valueOf(fields, "studentName"));
        contest.setContestName(contestName);
        contest.setOrganizer(valueOf(fields, "organizer"));
        contest.setContestCategory(valueOf(fields, "contestCategory"));
        contest.setAwardCategory(valueOf(fields, "awardCategory"));
        contest.setAwardLevel(valueOf(fields, "awardLevel"));
        contest.setContestType(valueOf(fields, "contestType"));
        contest.setAwardDate(parseDate(valueOf(fields, "awardDate")));
        contest.setAwardCount(valueOf(fields, "awardCount"));
        contest.setTeamMembers(valueOf(fields, "teamMembers"));
        contest.setInstructors(valueOf(fields, "instructors"));
        contest.setRemark(valueOf(fields, "remark"));
        contest.setImageUrl(imageUrl);
        contest.setImageUrls(valueOf(fields, "_imageUrls"));
        contest.setAttachments(valueOf(fields, "_attachments"));
        contest.setCreatedAt(LocalDateTime.now());
        return achievementContestRepository.save(contest);
    }

    private AchievementContest updateContest(String username, String role, Long id, Map<String, String> fields, String imageUrl) {
        AchievementContest contest = loadContest(username, role, id);
        String contestName = valueOf(fields, "contestName");
        if (!contestName.isEmpty()) {
            contest.setContestName(contestName);
        }
        contest.setStudentNo(valueOf(fields, "studentNo"));
        contest.setStudentName(valueOf(fields, "studentName"));
        contest.setOrganizer(valueOf(fields, "organizer"));
        contest.setContestCategory(valueOf(fields, "contestCategory"));
        contest.setAwardCategory(valueOf(fields, "awardCategory"));
        contest.setAwardLevel(valueOf(fields, "awardLevel"));
        contest.setContestType(valueOf(fields, "contestType"));
        contest.setAwardDate(parseDate(valueOf(fields, "awardDate")));
        contest.setAwardCount(valueOf(fields, "awardCount"));
        contest.setTeamMembers(valueOf(fields, "teamMembers"));
        contest.setInstructors(valueOf(fields, "instructors"));
        contest.setRemark(valueOf(fields, "remark"));
        contest.setImageUrl(imageUrl);
        contest.setImageUrls(valueOf(fields, "_imageUrls"));
        contest.setAttachments(valueOf(fields, "_attachments"));
        return achievementContestRepository.save(contest);
    }

    private AchievementPaper savePaper(AppUser author, Map<String, String> fields, String imageUrl) {
        String paperTitle = valueOf(fields, "paperTitle");
        if (paperTitle.isEmpty()) {
            throw new IllegalArgumentException("论文名称不能为空");
        }
        AchievementPaper paper = new AchievementPaper();
        paper.setAuthor(author);
        paper.setStudentNo(valueOf(fields, "studentNo"));
        paper.setStudentName(valueOf(fields, "studentName"));
        paper.setPaperTitle(paperTitle);
        paper.setJournalName(valueOf(fields, "journalName"));
        paper.setPublishDate(parseDate(valueOf(fields, "publishDate")));
        paper.setAuthorOrder(valueOf(fields, "authorOrder"));
        paper.setIndexed(valueOf(fields, "indexed"));
        paper.setRemark(valueOf(fields, "remark"));
        paper.setImageUrl(imageUrl);
        paper.setImageUrls(valueOf(fields, "_imageUrls"));
        paper.setAttachments(valueOf(fields, "_attachments"));
        paper.setCreatedAt(LocalDateTime.now());
        return achievementPaperRepository.save(paper);
    }

    private AchievementPaper updatePaper(String username, String role, Long id, Map<String, String> fields, String imageUrl) {
        AchievementPaper paper = loadPaper(username, role, id);
        String paperTitle = valueOf(fields, "paperTitle");
        if (!paperTitle.isEmpty()) {
            paper.setPaperTitle(paperTitle);
        }
        paper.setStudentNo(valueOf(fields, "studentNo"));
        paper.setStudentName(valueOf(fields, "studentName"));
        paper.setJournalName(valueOf(fields, "journalName"));
        paper.setPublishDate(parseDate(valueOf(fields, "publishDate")));
        paper.setAuthorOrder(valueOf(fields, "authorOrder"));
        paper.setIndexed(valueOf(fields, "indexed"));
        paper.setRemark(valueOf(fields, "remark"));
        paper.setImageUrl(imageUrl);
        paper.setImageUrls(valueOf(fields, "_imageUrls"));
        paper.setAttachments(valueOf(fields, "_attachments"));
        return achievementPaperRepository.save(paper);
    }

    private AchievementJournal saveJournal(AppUser author, Map<String, String> fields, String imageUrl) {
        String workTitle = valueOf(fields, "workTitle");
        if (workTitle.isEmpty()) {
            throw new IllegalArgumentException("作品名称不能为空");
        }
        AchievementJournal journal = new AchievementJournal();
        journal.setAuthor(author);
        journal.setStudentNo(valueOf(fields, "studentNo"));
        journal.setStudentName(valueOf(fields, "studentName"));
        journal.setWorkTitle(workTitle);
        journal.setPublicationName(valueOf(fields, "publicationName"));
        journal.setPublishDate(parseDate(valueOf(fields, "publishDate")));
        journal.setRemark(valueOf(fields, "remark"));
        journal.setImageUrl(imageUrl);
        journal.setImageUrls(valueOf(fields, "_imageUrls"));
        journal.setAttachments(valueOf(fields, "_attachments"));
        journal.setCreatedAt(LocalDateTime.now());
        return achievementJournalRepository.save(journal);
    }

    private AchievementJournal updateJournal(String username, String role, Long id, Map<String, String> fields, String imageUrl) {
        AchievementJournal journal = loadJournal(username, role, id);
        String workTitle = valueOf(fields, "workTitle");
        if (!workTitle.isEmpty()) {
            journal.setWorkTitle(workTitle);
        }
        journal.setStudentNo(valueOf(fields, "studentNo"));
        journal.setStudentName(valueOf(fields, "studentName"));
        journal.setPublicationName(valueOf(fields, "publicationName"));
        journal.setPublishDate(parseDate(valueOf(fields, "publishDate")));
        journal.setRemark(valueOf(fields, "remark"));
        journal.setImageUrl(imageUrl);
        journal.setImageUrls(valueOf(fields, "_imageUrls"));
        journal.setAttachments(valueOf(fields, "_attachments"));
        return achievementJournalRepository.save(journal);
    }

    private AchievementPatent savePatent(AppUser author, Map<String, String> fields, String imageUrl) {
        String patentName = valueOf(fields, "patentName");
        if (patentName.isEmpty()) {
            throw new IllegalArgumentException("名称不能为空");
        }
        AchievementPatent patent = new AchievementPatent();
        patent.setAuthor(author);
        patent.setStudentNo(valueOf(fields, "studentNo"));
        patent.setStudentName(valueOf(fields, "studentName"));
        patent.setPatentName(patentName);
        patent.setPatentType(valueOf(fields, "patentType"));
        patent.setGrantNo(valueOf(fields, "grantNo"));
        patent.setGrantDate(parseDate(valueOf(fields, "grantDate")));
        patent.setFirstInventor(valueOf(fields, "firstInventor"));
        patent.setRemark(valueOf(fields, "remark"));
        patent.setImageUrl(imageUrl);
        patent.setImageUrls(valueOf(fields, "_imageUrls"));
        patent.setAttachments(valueOf(fields, "_attachments"));
        patent.setCreatedAt(LocalDateTime.now());
        return achievementPatentRepository.save(patent);
    }

    private AchievementPatent updatePatent(String username, String role, Long id, Map<String, String> fields, String imageUrl) {
        AchievementPatent patent = loadPatent(username, role, id);
        String patentName = valueOf(fields, "patentName");
        if (!patentName.isEmpty()) {
            patent.setPatentName(patentName);
        }
        patent.setStudentNo(valueOf(fields, "studentNo"));
        patent.setStudentName(valueOf(fields, "studentName"));
        patent.setPatentType(valueOf(fields, "patentType"));
        patent.setGrantNo(valueOf(fields, "grantNo"));
        patent.setGrantDate(parseDate(valueOf(fields, "grantDate")));
        patent.setFirstInventor(valueOf(fields, "firstInventor"));
        patent.setRemark(valueOf(fields, "remark"));
        patent.setImageUrl(imageUrl);
        patent.setImageUrls(valueOf(fields, "_imageUrls"));
        patent.setAttachments(valueOf(fields, "_attachments"));
        return achievementPatentRepository.save(patent);
    }

    private AchievementCertificate saveCertificate(AppUser author, Map<String, String> fields, String imageUrl) {
        String certificateName = valueOf(fields, "certificateName");
        if (certificateName.isEmpty()) {
            throw new IllegalArgumentException("证书名称不能为空");
        }
        AchievementCertificate certificate = new AchievementCertificate();
        certificate.setAuthor(author);
        certificate.setStudentNo(valueOf(fields, "studentNo"));
        certificate.setStudentName(valueOf(fields, "studentName"));
        certificate.setCertificateType(valueOf(fields, "certificateType"));
        certificate.setCertificateName(certificateName);
        certificate.setObtainDate(parseDate(valueOf(fields, "obtainDate")));
        certificate.setRemark(valueOf(fields, "remark"));
        certificate.setImageUrl(imageUrl);
        certificate.setImageUrls(valueOf(fields, "_imageUrls"));
        certificate.setAttachments(valueOf(fields, "_attachments"));
        certificate.setCreatedAt(LocalDateTime.now());
        return achievementCertificateRepository.save(certificate);
    }

    private AchievementCertificate updateCertificate(String username, String role, Long id, Map<String, String> fields, String imageUrl) {
        AchievementCertificate certificate = loadCertificate(username, role, id);
        String certificateName = valueOf(fields, "certificateName");
        if (!certificateName.isEmpty()) {
            certificate.setCertificateName(certificateName);
        }
        certificate.setStudentNo(valueOf(fields, "studentNo"));
        certificate.setStudentName(valueOf(fields, "studentName"));
        certificate.setCertificateType(valueOf(fields, "certificateType"));
        certificate.setObtainDate(parseDate(valueOf(fields, "obtainDate")));
        certificate.setRemark(valueOf(fields, "remark"));
        certificate.setImageUrl(imageUrl);
        certificate.setImageUrls(valueOf(fields, "_imageUrls"));
        certificate.setAttachments(valueOf(fields, "_attachments"));
        return achievementCertificateRepository.save(certificate);
    }

    private AchievementResearch saveResearch(AppUser author, Map<String, String> fields, String imageUrl) {
        String projectName = valueOf(fields, "projectName");
        if (projectName.isEmpty()) {
            throw new IllegalArgumentException("参与科研项目名称不能为空");
        }
        AchievementResearch research = new AchievementResearch();
        research.setAuthor(author);
        research.setStudentNo(valueOf(fields, "studentNo"));
        research.setStudentName(valueOf(fields, "studentName"));
        research.setProjectName(projectName);
        research.setTeacherNo(valueOf(fields, "teacherNo"));
        research.setProjectLeader(valueOf(fields, "projectLeader"));
        research.setRemark(valueOf(fields, "remark"));
        research.setImageUrl(imageUrl);
        research.setImageUrls(valueOf(fields, "_imageUrls"));
        research.setAttachments(valueOf(fields, "_attachments"));
        research.setCreatedAt(LocalDateTime.now());
        return achievementResearchRepository.save(research);
    }

    private AchievementResearch updateResearch(String username, String role, Long id, Map<String, String> fields, String imageUrl) {
        AchievementResearch research = loadResearch(username, role, id);
        String projectName = valueOf(fields, "projectName");
        if (!projectName.isEmpty()) {
            research.setProjectName(projectName);
        }
        research.setStudentNo(valueOf(fields, "studentNo"));
        research.setStudentName(valueOf(fields, "studentName"));
        research.setTeacherNo(valueOf(fields, "teacherNo"));
        research.setProjectLeader(valueOf(fields, "projectLeader"));
        research.setRemark(valueOf(fields, "remark"));
        research.setImageUrl(imageUrl);
        research.setImageUrls(valueOf(fields, "_imageUrls"));
        research.setAttachments(valueOf(fields, "_attachments"));
        return achievementResearchRepository.save(research);
    }

    private AchievementWorks saveWorks(AppUser author, Map<String, String> fields, String imageUrl) {
        String workName = valueOf(fields, "workName");
        if (workName.isEmpty()) {
            throw new IllegalArgumentException("作品名称不能为空");
        }
        AchievementWorks works = new AchievementWorks();
        works.setAuthor(author);
        works.setStudentNo(valueOf(fields, "studentNo"));
        works.setStudentName(valueOf(fields, "studentName"));
        works.setWorkName(workName);
        works.setWorkCategory(valueOf(fields, "workCategory"));
        works.setWorkType(valueOf(fields, "workType"));
        works.setPublishDate(parseDate(valueOf(fields, "publishDate")));
        works.setPublishOccasion(valueOf(fields, "publishOccasion"));
        works.setOrganizer(valueOf(fields, "organizer"));
        works.setImpactScope(valueOf(fields, "impactScope"));
        works.setNote(valueOf(fields, "note"));
        works.setImageUrl(imageUrl);
        works.setImageUrls(valueOf(fields, "_imageUrls"));
        works.setAttachments(valueOf(fields, "_attachments"));
        works.setCreatedAt(LocalDateTime.now());
        return achievementWorksRepository.save(works);
    }

    private AchievementWorks updateWorks(String username, String role, Long id, Map<String, String> fields, String imageUrl) {
        AchievementWorks works = loadWorks(username, role, id);
        String workName = valueOf(fields, "workName");
        if (!workName.isEmpty()) {
            works.setWorkName(workName);
        }
        works.setStudentNo(valueOf(fields, "studentNo"));
        works.setStudentName(valueOf(fields, "studentName"));
        works.setWorkCategory(valueOf(fields, "workCategory"));
        works.setWorkType(valueOf(fields, "workType"));
        works.setPublishDate(parseDate(valueOf(fields, "publishDate")));
        works.setPublishOccasion(valueOf(fields, "publishOccasion"));
        works.setOrganizer(valueOf(fields, "organizer"));
        works.setImpactScope(valueOf(fields, "impactScope"));
        works.setNote(valueOf(fields, "note"));
        works.setImageUrl(imageUrl);
        works.setImageUrls(valueOf(fields, "_imageUrls"));
        works.setAttachments(valueOf(fields, "_attachments"));
        return achievementWorksRepository.save(works);
    }

    private AchievementDoubleHundred saveDoubleHundred(AppUser author, Map<String, String> fields, String imageUrl) {
        String projectName = valueOf(fields, "projectName");
        if (projectName.isEmpty()) {
            throw new IllegalArgumentException("申报作品名不能为空");
        }
        AchievementDoubleHundred doubleHundred = new AchievementDoubleHundred();
        doubleHundred.setAuthor(author);
        doubleHundred.setStudentNo(valueOf(fields, "studentNo"));
        doubleHundred.setStudentName(valueOf(fields, "studentName"));
        doubleHundred.setProjectCategory(valueOf(fields, "projectCategory"));
        doubleHundred.setProjectDomain(valueOf(fields, "projectDomain"));
        doubleHundred.setProjectName(projectName);
        doubleHundred.setProjectLeader(valueOf(fields, "projectLeader"));
        doubleHundred.setLeaderStudentNo(valueOf(fields, "leaderStudentNo"));
        doubleHundred.setEducationLevel(valueOf(fields, "educationLevel"));
        doubleHundred.setTeamMembers(valueOf(fields, "teamMembers"));
        doubleHundred.setInstructors(valueOf(fields, "instructors"));
        doubleHundred.setTeamSize(valueOf(fields, "teamSize"));
        doubleHundred.setPlannedLevel(valueOf(fields, "plannedLevel"));
        doubleHundred.setCollege(valueOf(fields, "college"));
        doubleHundred.setFinalLevel(valueOf(fields, "finalLevel"));
        doubleHundred.setRemark(valueOf(fields, "remark"));
        doubleHundred.setImageUrl(imageUrl);
        doubleHundred.setImageUrls(valueOf(fields, "_imageUrls"));
        doubleHundred.setAttachments(valueOf(fields, "_attachments"));
        doubleHundred.setCreatedAt(LocalDateTime.now());
        return achievementDoubleHundredRepository.save(doubleHundred);
    }

    private AchievementDoubleHundred updateDoubleHundred(String username, String role, Long id, Map<String, String> fields, String imageUrl) {
        AchievementDoubleHundred doubleHundred = loadDoubleHundred(username, role, id);
        String projectName = valueOf(fields, "projectName");
        if (!projectName.isEmpty()) {
            doubleHundred.setProjectName(projectName);
        }
        doubleHundred.setStudentNo(valueOf(fields, "studentNo"));
        doubleHundred.setStudentName(valueOf(fields, "studentName"));
        doubleHundred.setProjectCategory(valueOf(fields, "projectCategory"));
        doubleHundred.setProjectDomain(valueOf(fields, "projectDomain"));
        doubleHundred.setProjectLeader(valueOf(fields, "projectLeader"));
        doubleHundred.setLeaderStudentNo(valueOf(fields, "leaderStudentNo"));
        doubleHundred.setEducationLevel(valueOf(fields, "educationLevel"));
        doubleHundred.setTeamMembers(valueOf(fields, "teamMembers"));
        doubleHundred.setInstructors(valueOf(fields, "instructors"));
        doubleHundred.setTeamSize(valueOf(fields, "teamSize"));
        doubleHundred.setPlannedLevel(valueOf(fields, "plannedLevel"));
        doubleHundred.setCollege(valueOf(fields, "college"));
        doubleHundred.setFinalLevel(valueOf(fields, "finalLevel"));
        doubleHundred.setRemark(valueOf(fields, "remark"));
        doubleHundred.setImageUrl(imageUrl);
        doubleHundred.setImageUrls(valueOf(fields, "_imageUrls"));
        doubleHundred.setAttachments(valueOf(fields, "_attachments"));
        return achievementDoubleHundredRepository.save(doubleHundred);
    }

    private AchievementIeerTraining saveIeerTraining(AppUser author, Map<String, String> fields, String imageUrl) {
        String projectName = valueOf(fields, "projectName");
        if (projectName.isEmpty()) {
            throw new IllegalArgumentException("项目名称不能为空");
        }
        AchievementIeerTraining ieerTraining = new AchievementIeerTraining();
        ieerTraining.setAuthor(author);
        ieerTraining.setStudentNo(valueOf(fields, "studentNo"));
        ieerTraining.setStudentName(valueOf(fields, "studentName"));
        ieerTraining.setCollegeName(valueOf(fields, "collegeName"));
        ieerTraining.setProjectName(projectName);
        ieerTraining.setProjectType(valueOf(fields, "projectType"));
        ieerTraining.setProjectLeader(valueOf(fields, "projectLeader"));
        ieerTraining.setInstructorName(valueOf(fields, "instructorName"));
        ieerTraining.setRecommendedLevel(valueOf(fields, "recommendedLevel"));
        ieerTraining.setIsKeyArea(valueOf(fields, "isKeyArea"));
        ieerTraining.setFinalStatus(valueOf(fields, "finalStatus"));
        ieerTraining.setRemark(valueOf(fields, "remark"));
        ieerTraining.setImageUrl(imageUrl);
        ieerTraining.setImageUrls(valueOf(fields, "_imageUrls"));
        ieerTraining.setAttachments(valueOf(fields, "_attachments"));
        ieerTraining.setCreatedAt(LocalDateTime.now());
        return achievementIeerTrainingRepository.save(ieerTraining);
    }

    private AchievementIeerTraining updateIeerTraining(String username, String role, Long id, Map<String, String> fields, String imageUrl) {
        AchievementIeerTraining ieerTraining = loadIeerTraining(username, role, id);
        String projectName = valueOf(fields, "projectName");
        if (!projectName.isEmpty()) {
            ieerTraining.setProjectName(projectName);
        }
        ieerTraining.setStudentNo(valueOf(fields, "studentNo"));
        ieerTraining.setStudentName(valueOf(fields, "studentName"));
        ieerTraining.setCollegeName(valueOf(fields, "collegeName"));
        ieerTraining.setProjectType(valueOf(fields, "projectType"));
        ieerTraining.setProjectLeader(valueOf(fields, "projectLeader"));
        ieerTraining.setInstructorName(valueOf(fields, "instructorName"));
        ieerTraining.setRecommendedLevel(valueOf(fields, "recommendedLevel"));
        ieerTraining.setIsKeyArea(valueOf(fields, "isKeyArea"));
        ieerTraining.setFinalStatus(valueOf(fields, "finalStatus"));
        ieerTraining.setRemark(valueOf(fields, "remark"));
        ieerTraining.setImageUrl(imageUrl);
        ieerTraining.setImageUrls(valueOf(fields, "_imageUrls"));
        ieerTraining.setAttachments(valueOf(fields, "_attachments"));
        return achievementIeerTrainingRepository.save(ieerTraining);
    }

    private AchievementRecordResponse toResponse(AchievementContest contest) {
        Map<String, String> fields = new HashMap<>();
        fields.put("studentNo", contest.getStudentNo());
        fields.put("studentName", contest.getStudentName());
        fields.put("contestName", contest.getContestName());
        fields.put("organizer", contest.getOrganizer());
        fields.put("contestCategory", contest.getContestCategory());
        fields.put("awardCategory", contest.getAwardCategory());
        fields.put("awardLevel", contest.getAwardLevel());
        fields.put("contestType", contest.getContestType());
        fields.put("awardDate", formatDate(contest.getAwardDate()));
        fields.put("awardCount", contest.getAwardCount());
        fields.put("teamMembers", contest.getTeamMembers());
        fields.put("instructors", contest.getInstructors());
        fields.put("remark", contest.getRemark());
        if (contest.getImageUrls() != null) {
            fields.put("_imageUrls", contest.getImageUrls());
        }
        if (contest.getAttachments() != null) {
            fields.put("_attachments", contest.getAttachments());
        }
        return new AchievementRecordResponse(
            contest.getId(),
            "contest",
            contest.getImageUrl(),
            contest.getCreatedAt(),
            fields
        );
    }

    private AchievementRecordResponse toResponse(AchievementPaper paper) {
        Map<String, String> fields = new HashMap<>();
        fields.put("studentNo", paper.getStudentNo());
        fields.put("studentName", paper.getStudentName());
        fields.put("paperTitle", paper.getPaperTitle());
        fields.put("journalName", paper.getJournalName());
        fields.put("publishDate", formatDate(paper.getPublishDate()));
        fields.put("authorOrder", paper.getAuthorOrder());
        fields.put("indexed", paper.getIndexed());
        fields.put("remark", paper.getRemark());
        if (paper.getImageUrls() != null) {
            fields.put("_imageUrls", paper.getImageUrls());
        }
        if (paper.getAttachments() != null) {
            fields.put("_attachments", paper.getAttachments());
        }
        return new AchievementRecordResponse(
            paper.getId(),
            "paper",
            paper.getImageUrl(),
            paper.getCreatedAt(),
            fields
        );
    }

    private AchievementRecordResponse toResponse(AchievementJournal journal) {
        Map<String, String> fields = new HashMap<>();
        fields.put("studentNo", journal.getStudentNo());
        fields.put("studentName", journal.getStudentName());
        fields.put("workTitle", journal.getWorkTitle());
        fields.put("publicationName", journal.getPublicationName());
        fields.put("publishDate", formatDate(journal.getPublishDate()));
        fields.put("remark", journal.getRemark());
        if (journal.getImageUrls() != null) {
            fields.put("_imageUrls", journal.getImageUrls());
        }
        if (journal.getAttachments() != null) {
            fields.put("_attachments", journal.getAttachments());
        }
        return new AchievementRecordResponse(
            journal.getId(),
            "journal",
            journal.getImageUrl(),
            journal.getCreatedAt(),
            fields
        );
    }

    private AchievementRecordResponse toResponse(AchievementPatent patent) {
        Map<String, String> fields = new HashMap<>();
        fields.put("studentNo", patent.getStudentNo());
        fields.put("studentName", patent.getStudentName());
        fields.put("patentName", patent.getPatentName());
        fields.put("patentType", patent.getPatentType());
        fields.put("grantNo", patent.getGrantNo());
        fields.put("grantDate", formatDate(patent.getGrantDate()));
        fields.put("firstInventor", patent.getFirstInventor());
        fields.put("remark", patent.getRemark());
        if (patent.getImageUrls() != null) {
            fields.put("_imageUrls", patent.getImageUrls());
        }
        if (patent.getAttachments() != null) {
            fields.put("_attachments", patent.getAttachments());
        }
        return new AchievementRecordResponse(
            patent.getId(),
            "patent",
            patent.getImageUrl(),
            patent.getCreatedAt(),
            fields
        );
    }

    private AchievementRecordResponse toResponse(AchievementCertificate certificate) {
        Map<String, String> fields = new HashMap<>();
        fields.put("studentNo", certificate.getStudentNo());
        fields.put("studentName", certificate.getStudentName());
        fields.put("certificateType", certificate.getCertificateType());
        fields.put("certificateName", certificate.getCertificateName());
        fields.put("obtainDate", formatDate(certificate.getObtainDate()));
        fields.put("remark", certificate.getRemark());
        if (certificate.getImageUrls() != null) {
            fields.put("_imageUrls", certificate.getImageUrls());
        }
        if (certificate.getAttachments() != null) {
            fields.put("_attachments", certificate.getAttachments());
        }
        return new AchievementRecordResponse(
            certificate.getId(),
            "certificate",
            certificate.getImageUrl(),
            certificate.getCreatedAt(),
            fields
        );
    }

    private AchievementRecordResponse toResponse(AchievementResearch research) {
        Map<String, String> fields = new HashMap<>();
        fields.put("studentNo", research.getStudentNo());
        fields.put("studentName", research.getStudentName());
        fields.put("projectName", research.getProjectName());
        fields.put("teacherNo", research.getTeacherNo());
        fields.put("projectLeader", research.getProjectLeader());
        fields.put("remark", research.getRemark());
        if (research.getImageUrls() != null) {
            fields.put("_imageUrls", research.getImageUrls());
        }
        if (research.getAttachments() != null) {
            fields.put("_attachments", research.getAttachments());
        }
        return new AchievementRecordResponse(
            research.getId(),
            "research",
            research.getImageUrl(),
            research.getCreatedAt(),
            fields
        );
    }

    private AchievementRecordResponse toResponse(AchievementWorks works) {
        Map<String, String> fields = new HashMap<>();
        fields.put("studentNo", works.getStudentNo());
        fields.put("studentName", works.getStudentName());
        fields.put("workName", works.getWorkName());
        fields.put("workCategory", works.getWorkCategory());
        fields.put("workType", works.getWorkType());
        fields.put("publishDate", formatDate(works.getPublishDate()));
        fields.put("publishOccasion", works.getPublishOccasion());
        fields.put("organizer", works.getOrganizer());
        fields.put("impactScope", works.getImpactScope());
        fields.put("note", works.getNote());
        if (works.getImageUrls() != null) {
            fields.put("_imageUrls", works.getImageUrls());
        }
        if (works.getAttachments() != null) {
            fields.put("_attachments", works.getAttachments());
        }
        return new AchievementRecordResponse(
            works.getId(),
            "works",
            works.getImageUrl(),
            works.getCreatedAt(),
            fields
        );
    }

    private AchievementRecordResponse toResponse(AchievementDoubleHundred doubleHundred) {
        Map<String, String> fields = new HashMap<>();
        fields.put("studentNo", doubleHundred.getStudentNo());
        fields.put("studentName", doubleHundred.getStudentName());
        fields.put("projectCategory", doubleHundred.getProjectCategory());
        fields.put("projectDomain", doubleHundred.getProjectDomain());
        fields.put("projectName", doubleHundred.getProjectName());
        fields.put("projectLeader", doubleHundred.getProjectLeader());
        fields.put("leaderStudentNo", doubleHundred.getLeaderStudentNo());
        fields.put("educationLevel", doubleHundred.getEducationLevel());
        fields.put("teamMembers", doubleHundred.getTeamMembers());
        fields.put("instructors", doubleHundred.getInstructors());
        fields.put("teamSize", doubleHundred.getTeamSize());
        fields.put("plannedLevel", doubleHundred.getPlannedLevel());
        fields.put("college", doubleHundred.getCollege());
        fields.put("finalLevel", doubleHundred.getFinalLevel());
        fields.put("remark", doubleHundred.getRemark());
        if (doubleHundred.getImageUrls() != null) {
            fields.put("_imageUrls", doubleHundred.getImageUrls());
        }
        if (doubleHundred.getAttachments() != null) {
            fields.put("_attachments", doubleHundred.getAttachments());
        }
        return new AchievementRecordResponse(
            doubleHundred.getId(),
            "doubleHundred",
            doubleHundred.getImageUrl(),
            doubleHundred.getCreatedAt(),
            fields
        );
    }

    private AchievementRecordResponse toResponse(AchievementIeerTraining ieerTraining) {
        Map<String, String> fields = new HashMap<>();
        fields.put("studentNo", ieerTraining.getStudentNo());
        fields.put("studentName", ieerTraining.getStudentName());
        fields.put("collegeName", ieerTraining.getCollegeName());
        fields.put("projectName", ieerTraining.getProjectName());
        fields.put("projectType", ieerTraining.getProjectType());
        fields.put("projectLeader", ieerTraining.getProjectLeader());
        fields.put("instructorName", ieerTraining.getInstructorName());
        fields.put("recommendedLevel", ieerTraining.getRecommendedLevel());
        fields.put("isKeyArea", ieerTraining.getIsKeyArea());
        fields.put("finalStatus", ieerTraining.getFinalStatus());
        fields.put("remark", ieerTraining.getRemark());
        if (ieerTraining.getImageUrls() != null) {
            fields.put("_imageUrls", ieerTraining.getImageUrls());
        }
        if (ieerTraining.getAttachments() != null) {
            fields.put("_attachments", ieerTraining.getAttachments());
        }
        return new AchievementRecordResponse(
            ieerTraining.getId(),
            "ieerTraining",
            ieerTraining.getImageUrl(),
            ieerTraining.getCreatedAt(),
            fields
        );
    }

    private String valueOf(Map<String, String> fields, String key) {
        String value = fields.get(key);
        return value == null ? "" : value.trim();
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.toString();
    }
}
