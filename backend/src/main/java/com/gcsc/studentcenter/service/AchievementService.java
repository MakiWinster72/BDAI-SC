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

    public AchievementService(
        AppUserRepository appUserRepository,
        AchievementContestRepository achievementContestRepository,
        AchievementPaperRepository achievementPaperRepository,
        AchievementJournalRepository achievementJournalRepository,
        AchievementPatentRepository achievementPatentRepository,
        AchievementCertificateRepository achievementCertificateRepository,
        AchievementResearchRepository achievementResearchRepository,
        AchievementWorksRepository achievementWorksRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.achievementContestRepository = achievementContestRepository;
        this.achievementPaperRepository = achievementPaperRepository;
        this.achievementJournalRepository = achievementJournalRepository;
        this.achievementPatentRepository = achievementPatentRepository;
        this.achievementCertificateRepository = achievementCertificateRepository;
        this.achievementResearchRepository = achievementResearchRepository;
        this.achievementWorksRepository = achievementWorksRepository;
    }

    public List<AchievementRecordResponse> list(String username, String category) {
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
            default:
                throw new IllegalArgumentException("无效的成就分类");
        }
    }

    public AchievementRecordResponse getById(String username, String category, Long id) {
        switch (requireCategory(category)) {
            case "contest":
                return toResponse(loadContest(username, id));
            case "paper":
                return toResponse(loadPaper(username, id));
            case "journal":
                return toResponse(loadJournal(username, id));
            case "patent":
                return toResponse(loadPatent(username, id));
            case "certificate":
                return toResponse(loadCertificate(username, id));
            case "research":
                return toResponse(loadResearch(username, id));
            case "works":
                return toResponse(loadWorks(username, id));
            default:
                throw new IllegalArgumentException("无效的成就分类");
        }
    }

    public AchievementRecordResponse create(String username, String category, AchievementRecordRequest request) {
        AppUser author = appUserRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Map<String, String> fields = safeFields(request.getFields());
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
            default:
                throw new IllegalArgumentException("无效的成就分类");
        }
    }

    public AchievementRecordResponse update(String username, String category, Long id, AchievementRecordRequest request) {
        Map<String, String> fields = safeFields(request.getFields());
        switch (requireCategory(category)) {
            case "contest":
                return toResponse(updateContest(username, id, fields, request.getImageUrl()));
            case "paper":
                return toResponse(updatePaper(username, id, fields, request.getImageUrl()));
            case "journal":
                return toResponse(updateJournal(username, id, fields, request.getImageUrl()));
            case "patent":
                return toResponse(updatePatent(username, id, fields, request.getImageUrl()));
            case "certificate":
                return toResponse(updateCertificate(username, id, fields, request.getImageUrl()));
            case "research":
                return toResponse(updateResearch(username, id, fields, request.getImageUrl()));
            case "works":
                return toResponse(updateWorks(username, id, fields, request.getImageUrl()));
            default:
                throw new IllegalArgumentException("无效的成就分类");
        }
    }

    public void delete(String username, String category, Long id) {
        switch (requireCategory(category)) {
            case "contest":
                achievementContestRepository.delete(loadContest(username, id));
                return;
            case "paper":
                achievementPaperRepository.delete(loadPaper(username, id));
                return;
            case "journal":
                achievementJournalRepository.delete(loadJournal(username, id));
                return;
            case "patent":
                achievementPatentRepository.delete(loadPatent(username, id));
                return;
            case "certificate":
                achievementCertificateRepository.delete(loadCertificate(username, id));
                return;
            case "research":
                achievementResearchRepository.delete(loadResearch(username, id));
                return;
            case "works":
                achievementWorksRepository.delete(loadWorks(username, id));
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
        return all.stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .collect(Collectors.toList());
    }

    private AchievementContest loadContest(String username, Long id) {
        AchievementContest contest = achievementContestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("成就不存在"));
        ensureOwner(username, contest.getAuthor());
        return contest;
    }

    private AchievementPaper loadPaper(String username, Long id) {
        AchievementPaper paper = achievementPaperRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("成就不存在"));
        ensureOwner(username, paper.getAuthor());
        return paper;
    }

    private AchievementJournal loadJournal(String username, Long id) {
        AchievementJournal journal = achievementJournalRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("成就不存在"));
        ensureOwner(username, journal.getAuthor());
        return journal;
    }

    private AchievementPatent loadPatent(String username, Long id) {
        AchievementPatent patent = achievementPatentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("成就不存在"));
        ensureOwner(username, patent.getAuthor());
        return patent;
    }

    private AchievementCertificate loadCertificate(String username, Long id) {
        AchievementCertificate certificate = achievementCertificateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("成就不存在"));
        ensureOwner(username, certificate.getAuthor());
        return certificate;
    }

    private AchievementResearch loadResearch(String username, Long id) {
        AchievementResearch research = achievementResearchRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("成就不存在"));
        ensureOwner(username, research.getAuthor());
        return research;
    }

    private AchievementWorks loadWorks(String username, Long id) {
        AchievementWorks works = achievementWorksRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("成就不存在"));
        ensureOwner(username, works.getAuthor());
        return works;
    }

    private void ensureOwner(String username, AppUser author) {
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
        contest.setCreatedAt(LocalDateTime.now());
        return achievementContestRepository.save(contest);
    }

    private AchievementContest updateContest(String username, Long id, Map<String, String> fields, String imageUrl) {
        AchievementContest contest = loadContest(username, id);
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
        paper.setImageUrl(imageUrl);
        paper.setCreatedAt(LocalDateTime.now());
        return achievementPaperRepository.save(paper);
    }

    private AchievementPaper updatePaper(String username, Long id, Map<String, String> fields, String imageUrl) {
        AchievementPaper paper = loadPaper(username, id);
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
        paper.setImageUrl(imageUrl);
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
        journal.setImageUrl(imageUrl);
        journal.setCreatedAt(LocalDateTime.now());
        return achievementJournalRepository.save(journal);
    }

    private AchievementJournal updateJournal(String username, Long id, Map<String, String> fields, String imageUrl) {
        AchievementJournal journal = loadJournal(username, id);
        String workTitle = valueOf(fields, "workTitle");
        if (!workTitle.isEmpty()) {
            journal.setWorkTitle(workTitle);
        }
        journal.setStudentNo(valueOf(fields, "studentNo"));
        journal.setStudentName(valueOf(fields, "studentName"));
        journal.setPublicationName(valueOf(fields, "publicationName"));
        journal.setPublishDate(parseDate(valueOf(fields, "publishDate")));
        journal.setImageUrl(imageUrl);
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
        patent.setImageUrl(imageUrl);
        patent.setCreatedAt(LocalDateTime.now());
        return achievementPatentRepository.save(patent);
    }

    private AchievementPatent updatePatent(String username, Long id, Map<String, String> fields, String imageUrl) {
        AchievementPatent patent = loadPatent(username, id);
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
        patent.setImageUrl(imageUrl);
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
        certificate.setImageUrl(imageUrl);
        certificate.setCreatedAt(LocalDateTime.now());
        return achievementCertificateRepository.save(certificate);
    }

    private AchievementCertificate updateCertificate(String username, Long id, Map<String, String> fields, String imageUrl) {
        AchievementCertificate certificate = loadCertificate(username, id);
        String certificateName = valueOf(fields, "certificateName");
        if (!certificateName.isEmpty()) {
            certificate.setCertificateName(certificateName);
        }
        certificate.setStudentNo(valueOf(fields, "studentNo"));
        certificate.setStudentName(valueOf(fields, "studentName"));
        certificate.setCertificateType(valueOf(fields, "certificateType"));
        certificate.setObtainDate(parseDate(valueOf(fields, "obtainDate")));
        certificate.setImageUrl(imageUrl);
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
        research.setImageUrl(imageUrl);
        research.setCreatedAt(LocalDateTime.now());
        return achievementResearchRepository.save(research);
    }

    private AchievementResearch updateResearch(String username, Long id, Map<String, String> fields, String imageUrl) {
        AchievementResearch research = loadResearch(username, id);
        String projectName = valueOf(fields, "projectName");
        if (!projectName.isEmpty()) {
            research.setProjectName(projectName);
        }
        research.setStudentNo(valueOf(fields, "studentNo"));
        research.setStudentName(valueOf(fields, "studentName"));
        research.setTeacherNo(valueOf(fields, "teacherNo"));
        research.setProjectLeader(valueOf(fields, "projectLeader"));
        research.setImageUrl(imageUrl);
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
        works.setCreatedAt(LocalDateTime.now());
        return achievementWorksRepository.save(works);
    }

    private AchievementWorks updateWorks(String username, Long id, Map<String, String> fields, String imageUrl) {
        AchievementWorks works = loadWorks(username, id);
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
        return achievementWorksRepository.save(works);
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
        return new AchievementRecordResponse(
            works.getId(),
            "works",
            works.getImageUrl(),
            works.getCreatedAt(),
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
