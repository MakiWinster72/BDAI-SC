package com.gcsc.studentcenter.service;

import com.gcsc.studentcenter.dto.ReviewInboxPageResponse;
import com.gcsc.studentcenter.dto.ReviewRequestSummaryResponse;
import com.gcsc.studentcenter.dto.ReviewUserResponse;
import com.gcsc.studentcenter.entity.AchievementReviewRequest;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.ProfileReviewRequest;
import com.gcsc.studentcenter.entity.UserRole;
import com.gcsc.studentcenter.repository.AchievementReviewRequestRepository;
import com.gcsc.studentcenter.repository.AppUserRepository;
import com.gcsc.studentcenter.repository.ProfileReviewRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ReviewInboxService {

  private static final int MAX_PAGE_SIZE = 50;
  private static final List<String> ACHIEVEMENT_TARGET_ROLES = List.of("TEACHER", "ADMIN");
  private static final List<String> PROFILE_TARGET_ROLES = List.of("TEACHER", "ADMIN");

  private final ProfileReviewRequestRepository profileReviewRequestRepository;
  private final AchievementReviewRequestRepository achievementReviewRequestRepository;
  private final AppUserRepository appUserRepository;
  private final UserService userService;
  private final NotificationReadStateService notificationReadStateService;
  private final SystemSettingsService systemSettingsService;
  private final ProfileReviewRequestService profileReviewRequestService;
  private final AchievementReviewRequestService achievementReviewRequestService;

  public ReviewInboxService(
      ProfileReviewRequestRepository profileReviewRequestRepository,
      AchievementReviewRequestRepository achievementReviewRequestRepository,
      AppUserRepository appUserRepository,
      UserService userService,
      NotificationReadStateService notificationReadStateService,
      SystemSettingsService systemSettingsService,
      ProfileReviewRequestService profileReviewRequestService,
      AchievementReviewRequestService achievementReviewRequestService) {
    this.profileReviewRequestRepository = profileReviewRequestRepository;
    this.achievementReviewRequestRepository = achievementReviewRequestRepository;
    this.appUserRepository = appUserRepository;
    this.userService = userService;
    this.notificationReadStateService = notificationReadStateService;
    this.systemSettingsService = systemSettingsService;
    this.profileReviewRequestService = profileReviewRequestService;
    this.achievementReviewRequestService = achievementReviewRequestService;
  }

  @Transactional(readOnly = true)
  public ReviewInboxPageResponse listInbox(
      String username,
      int page,
      int size,
      String category,
      String search,
      String scope) {
    AppUser user = loadUser(username);
    int safePage = Math.max(page, 1);
    int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    String normalizedCategory = normalizeCategory(category);
    String normalizedScope = scope == null || scope.isBlank() ? "inbox" : scope.trim().toLowerCase(Locale.ROOT);
    String normalizedSearch = search == null ? "" : search.trim();

    long delayedThresholdDays = readDelayedThresholdDays();
    Set<Long> profileReadIds = notificationReadStateService.findReadResourceIds(
        username,
        NotificationReadStateService.RESOURCE_PROFILE);
    Set<Long> achievementReadIds = notificationReadStateService.findReadResourceIds(
        username,
        NotificationReadStateService.RESOURCE_ACHIEVEMENT);

    List<InboxRow> rows = new ArrayList<>();
    rows.addAll(buildProfileRows(user, username, profileReadIds, delayedThresholdDays, normalizedScope));
    rows.addAll(buildAchievementRows(user, username, achievementReadIds, delayedThresholdDays, normalizedScope));

    rows.sort(Comparator.comparing(InboxRow::sortAt).reversed());

    Map<String, Long> categoryCounts = countCategories(rows);

    List<InboxRow> filtered = rows.stream()
        .filter(row -> matchesCategory(row, normalizedCategory))
        .filter(row -> matchesSearch(row.summary(), normalizedSearch))
        .toList();

    long total = filtered.size();
    int pages = total == 0 ? 0 : (int) Math.ceil((double) total / safeSize);
    int fromIndex = Math.min((safePage - 1) * safeSize, filtered.size());
    int toIndex = Math.min(fromIndex + safeSize, filtered.size());
    List<ReviewRequestSummaryResponse> pageItems = filtered.subList(fromIndex, toIndex).stream()
        .map(InboxRow::summary)
        .toList();

    return new ReviewInboxPageResponse(pageItems, total, safePage, safeSize, pages, categoryCounts);
  }

  @Transactional(readOnly = true)
  public Map<String, Object> getInboxStats(String username, String scope) {
    ReviewInboxPageResponse all = listInbox(username, 1, 1, "pending", "", scope);
    Map<String, Long> counts = all.getCategoryCounts();
    boolean hasPendingProfile = profileReviewRequestRepository
        .findAllByRequester_UsernameOrderByUpdatedAtDesc(username)
        .stream()
        .anyMatch(request -> "pending".equals(request.getStatus()));
    return Map.of(
        "pending", counts.getOrDefault("pending", 0L),
        "delayed", counts.getOrDefault("delayed", 0L),
        "approved", counts.getOrDefault("approved", 0L),
        "rejected", counts.getOrDefault("rejected", 0L),
        "unread", counts.getOrDefault("unread", 0L),
        "hasPendingProfile", hasPendingProfile);
  }

  @Transactional(readOnly = true)
  public ReviewRequestSummaryResponse findPendingAchievement(
      String username,
      Long recordId,
      String category) {
    if (recordId == null || category == null || category.isBlank()) {
      return null;
    }
    AppUser user = loadUser(username);
    return achievementReviewRequestRepository.findAllByRequester_UsernameOrderByUpdatedAtDesc(username)
        .stream()
        .filter(request -> "pending".equals(request.getStatus()))
        .filter(request -> recordId.equals(request.getRecordId()))
        .filter(request -> category.equals(request.getCategory()))
        .filter(request -> isProfileOrAchievementVisible(user, username, request.getRequester(), request.getReviewer(),
            request.getStatus()))
        .findFirst()
        .map(request -> toAchievementSummary(
            request,
            notificationReadStateService.isRead(
                username,
                NotificationReadStateService.RESOURCE_ACHIEVEMENT,
                request.getId())))
        .orElse(null);
  }

  @Transactional(readOnly = true)
  public Object getInboxDetail(String username, String resourceType, Long id) {
    String normalizedType = resourceType == null ? "" : resourceType.trim().toLowerCase(Locale.ROOT);
    if ("profile".equals(normalizedType)) {
      return profileReviewRequestService.getVisibleRequest(username, id);
    }
    if ("achievement".equals(normalizedType)) {
      return achievementReviewRequestService.getVisibleRequest(username, id);
    }
    throw new IllegalArgumentException("审核请求类型无效");
  }

  private List<InboxRow> buildProfileRows(
      AppUser user,
      String username,
      Set<Long> readIds,
      long delayedThresholdDays,
      String scope) {
    List<ProfileReviewRequest> requests = isReviewer(user)
        ? profileReviewRequestRepository.findAllByOrderByUpdatedAtDesc()
        : profileReviewRequestRepository.findAllByRequester_UsernameOrderByUpdatedAtDesc(username);

    List<InboxRow> rows = new ArrayList<>();
    for (ProfileReviewRequest request : requests) {
      if (!isProfileVisible(user, username, request)) {
        continue;
      }
      if ("class-reviews".equals(scope) && !matchesClassReviewScope(user, request.getRequester())) {
        continue;
      }
      boolean read = readIds.contains(request.getId());
      ReviewRequestSummaryResponse summary = toProfileSummary(request, read);
      String categoryKey = resolveCategoryKey(request.getStatus(), request.getUpdatedAt(), read, delayedThresholdDays);
      rows.add(new InboxRow(summary, request.getUpdatedAt(), categoryKey));
    }
    return rows;
  }

  private List<InboxRow> buildAchievementRows(
      AppUser user,
      String username,
      Set<Long> readIds,
      long delayedThresholdDays,
      String scope) {
    List<AchievementReviewRequest> requests = isReviewer(user)
        ? achievementReviewRequestRepository.findAllByOrderByUpdatedAtDesc()
        : achievementReviewRequestRepository.findAllByRequester_UsernameOrderByUpdatedAtDesc(username);

    List<InboxRow> rows = new ArrayList<>();
    for (AchievementReviewRequest request : requests) {
      if (!isAchievementVisible(user, username, request)) {
        continue;
      }
      if ("class-reviews".equals(scope) && !matchesClassReviewScope(user, request.getRequester())) {
        continue;
      }
      boolean read = readIds.contains(request.getId());
      ReviewRequestSummaryResponse summary = toAchievementSummary(request, read);
      String categoryKey = resolveCategoryKey(request.getStatus(), request.getUpdatedAt(), read, delayedThresholdDays);
      rows.add(new InboxRow(summary, request.getUpdatedAt(), categoryKey));
    }
    return rows;
  }

  private boolean isProfileVisible(AppUser user, String username, ProfileReviewRequest request) {
    return isProfileOrAchievementVisible(
        user,
        username,
        request.getRequester(),
        request.getReviewer(),
        request.getStatus());
  }

  private boolean isAchievementVisible(AppUser user, String username, AchievementReviewRequest request) {
    return isProfileOrAchievementVisible(
        user,
        username,
        request.getRequester(),
        request.getReviewer(),
        request.getStatus());
  }

  private boolean isProfileOrAchievementVisible(
      AppUser user,
      String username,
      AppUser requester,
      AppUser reviewer,
      String status) {
    if ("pending".equals(status)) {
      if (requester.getUsername().equals(username)) {
        return true;
      }
      if (!isReviewer(user)) {
        return false;
      }
      if (user.getRole() == UserRole.ADMIN) {
        return true;
      }
      if (user.getRole() == UserRole.TEACHER) {
        return userService.isStudentInTeacherAssignedClass(user, requester);
      }
      if (user.getRole() == UserRole.CADRE) {
        return isStudentInCadreOwnClass(user, requester);
      }
      return true;
    }
    return requester.getUsername().equals(username)
        || (reviewer != null && reviewer.getUsername().equals(username));
  }

  private boolean matchesClassReviewScope(AppUser cadre, AppUser requester) {
    if (cadre.getRole() != UserRole.CADRE) {
      return false;
    }
    if (requester.getUsername().equals(cadre.getUsername())) {
      return false;
    }
    if (requester.getRole() == UserRole.CADRE) {
      return false;
    }
    String cadreClass = cadre.getClassName();
    String requesterClass = requester.getClassName();
    if (cadreClass == null || cadreClass.isBlank() || requesterClass == null || requesterClass.isBlank()) {
      return false;
    }
    return cadreClass.trim().equals(requesterClass.trim());
  }

  private boolean isStudentInCadreOwnClass(AppUser cadre, AppUser student) {
    String cadreClass = cadre.getClassName();
    if (cadreClass == null || cadreClass.isBlank()) {
      return false;
    }
    String studentClass = student.getClassName();
    if (studentClass == null || studentClass.isBlank()) {
      return false;
    }
    return cadreClass.trim().equals(studentClass.trim());
  }

  private ReviewRequestSummaryResponse toProfileSummary(ProfileReviewRequest request, boolean read) {
    return new ReviewRequestSummaryResponse(
        request.getId(),
        "profile",
        request.getStatus(),
        null,
        null,
        null,
        null,
        request.getTitle(),
        request.getSummary(),
        nullToEmpty(request.getRejectionReason()),
        toUserResponse(request.getRequester()),
        request.getReviewer() == null ? null : toUserResponse(request.getReviewer()),
        PROFILE_TARGET_ROLES,
        request.getCreatedAt(),
        request.getUpdatedAt(),
        read);
  }

  private ReviewRequestSummaryResponse toAchievementSummary(AchievementReviewRequest request, boolean read) {
    return new ReviewRequestSummaryResponse(
        request.getId(),
        "achievement",
        request.getStatus(),
        request.getAction(),
        request.getCategory(),
        resolveCategoryLabel(request.getCategory()),
        request.getRecordId(),
        request.getTitle(),
        request.getSummary(),
        nullToEmpty(request.getRejectionReason()),
        toUserResponse(request.getRequester()),
        request.getReviewer() == null ? null : toUserResponse(request.getReviewer()),
        ACHIEVEMENT_TARGET_ROLES,
        request.getCreatedAt(),
        request.getUpdatedAt(),
        read);
  }

  private ReviewUserResponse toUserResponse(AppUser user) {
    return new ReviewUserResponse(
        user.getUsername(),
        user.getDisplayName(),
        user.getRole() == null ? "" : user.getRole().name(),
        user.getStudentNo(),
        user.getClassName());
  }

  private String resolveCategoryKey(
      String status,
      LocalDateTime updatedAt,
      boolean read,
      long delayedThresholdDays) {
    if ("approved".equals(status)) {
      return "approved";
    }
    if ("rejected".equals(status)) {
      return "rejected";
    }
    if ("pending".equals(status)) {
      if (isDelayed(updatedAt, delayedThresholdDays)) {
        return "delayed";
      }
      return "pending";
    }
    return "pending";
  }

  private boolean matchesCategory(InboxRow row, String category) {
    if ("unread".equals(category)) {
      return !row.summary().isRead();
    }
    return category.equals(row.categoryKey());
  }

  private boolean matchesSearch(ReviewRequestSummaryResponse summary, String search) {
    if (search.isEmpty()) {
      return true;
    }
    String normalized = search.toLowerCase(Locale.ROOT);
    List<String> fields = List.of(
        summary.getTitle(),
        summary.getSummary(),
        summary.getRejectionReason());
    for (String field : fields) {
      if (field != null && field.toLowerCase(Locale.ROOT).contains(normalized)) {
        return true;
      }
    }
    String idStr = summary.getId() == null ? "" : String.valueOf(summary.getId());
    String numQuery = search.startsWith("#") ? search.substring(1).trim() : search;
    return idStr.contains(numQuery) || ("#" + idStr).toLowerCase(Locale.ROOT).contains(normalized);
  }

  private Map<String, Long> countCategories(List<InboxRow> rows) {
    Map<String, Long> counts = new LinkedHashMap<>();
    counts.put("unread", 0L);
    counts.put("pending", 0L);
    counts.put("delayed", 0L);
    counts.put("approved", 0L);
    counts.put("rejected", 0L);
    for (InboxRow row : rows) {
      if (!row.summary().isRead()) {
        counts.put("unread", counts.get("unread") + 1);
      }
      String key = row.categoryKey();
      counts.put(key, counts.getOrDefault(key, 0L) + 1);
    }
    return counts;
  }

  private boolean isDelayed(LocalDateTime updatedAt, long delayedThresholdDays) {
    if (updatedAt == null) {
      return false;
    }
    long days = ChronoUnit.DAYS.between(updatedAt, LocalDateTime.now());
    return days >= delayedThresholdDays;
  }

  private long readDelayedThresholdDays() {
    Object raw = systemSettingsService.getSettings().get("delayedThresholdDays");
    if (raw instanceof Number number) {
      long days = number.longValue();
      return days >= 1 ? days : 2L;
    }
    return 2L;
  }

  private String normalizeCategory(String category) {
    if (category == null || category.isBlank()) {
      return "pending";
    }
    String normalized = category.trim().toLowerCase(Locale.ROOT);
    return switch (normalized) {
      case "unread", "pending", "delayed", "approved", "rejected" -> normalized;
      default -> "pending";
    };
  }

  private boolean isReviewer(AppUser user) {
    return user.getRole() == UserRole.ADMIN
        || user.getRole() == UserRole.TEACHER
        || user.getRole() == UserRole.CADRE;
  }

  private AppUser loadUser(String username) {
    return appUserRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
  }

  private String nullToEmpty(String value) {
    return value == null ? "" : value;
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

  private record InboxRow(
      ReviewRequestSummaryResponse summary,
      LocalDateTime sortAt,
      String categoryKey) {
  }
}
