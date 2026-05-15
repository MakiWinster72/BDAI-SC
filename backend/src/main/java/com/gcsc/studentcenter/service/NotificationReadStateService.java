package com.gcsc.studentcenter.service;

import com.gcsc.studentcenter.dto.NotificationReadStateRequest;
import com.gcsc.studentcenter.entity.AchievementReviewRequest;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.NotificationReadState;
import com.gcsc.studentcenter.entity.ProfileReviewRequest;
import com.gcsc.studentcenter.entity.UserRole;
import com.gcsc.studentcenter.repository.AchievementReviewRequestRepository;
import com.gcsc.studentcenter.repository.AppUserRepository;
import com.gcsc.studentcenter.repository.NotificationReadStateRepository;
import com.gcsc.studentcenter.repository.ProfileReviewRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class NotificationReadStateService {

  public static final String RESOURCE_ACHIEVEMENT = "achievement";
  public static final String RESOURCE_PROFILE = "profile";

  private final NotificationReadStateRepository notificationReadStateRepository;
  private final AppUserRepository appUserRepository;
  private final AchievementReviewRequestRepository achievementReviewRequestRepository;
  private final ProfileReviewRequestRepository profileReviewRequestRepository;
  private final UserService userService;

  public NotificationReadStateService(
      NotificationReadStateRepository notificationReadStateRepository,
      AppUserRepository appUserRepository,
      AchievementReviewRequestRepository achievementReviewRequestRepository,
      ProfileReviewRequestRepository profileReviewRequestRepository,
      UserService userService) {
    this.notificationReadStateRepository = notificationReadStateRepository;
    this.appUserRepository = appUserRepository;
    this.achievementReviewRequestRepository = achievementReviewRequestRepository;
    this.profileReviewRequestRepository = profileReviewRequestRepository;
    this.userService = userService;
  }

  @Transactional(readOnly = true)
  public Set<Long> findReadResourceIds(String username, String resourceType) {
    String normalizedType = normalizeResourceType(resourceType);
    Set<Long> ids = new HashSet<>();
    notificationReadStateRepository.findAllByUser_UsernameAndResourceType(username, normalizedType)
        .forEach(state -> ids.add(state.getResourceId()));
    return ids;
  }

  @Transactional(readOnly = true)
  public boolean isRead(String username, String resourceType, Long resourceId) {
    AppUser user = loadUser(username);
    String normalizedType = normalizeResourceType(resourceType);
    Long normalizedId = requireResourceId(resourceId);
    return notificationReadStateRepository
        .findByUserAndResourceTypeAndResourceId(user, normalizedType, normalizedId)
        .isPresent();
  }

  @Transactional
  public void markRead(String username, NotificationReadStateRequest request) {
    AppUser user = loadUser(username);
    String resourceType = normalizeResourceType(request == null ? null : request.getResourceType());
    Long resourceId = requireResourceId(request == null ? null : request.getResourceId());
    ensureVisible(user, resourceType, resourceId);

    NotificationReadState state = notificationReadStateRepository
        .findByUserAndResourceTypeAndResourceId(user, resourceType, resourceId)
        .orElseGet(NotificationReadState::new);
    state.setUser(user);
    state.setResourceType(resourceType);
    state.setResourceId(resourceId);
    state.setReadAt(LocalDateTime.now());
    notificationReadStateRepository.save(state);
  }

  @Transactional
  public void markUnread(String username, NotificationReadStateRequest request) {
    AppUser user = loadUser(username);
    String resourceType = normalizeResourceType(request == null ? null : request.getResourceType());
    Long resourceId = requireResourceId(request == null ? null : request.getResourceId());
    ensureVisible(user, resourceType, resourceId);

    notificationReadStateRepository.findByUserAndResourceTypeAndResourceId(user, resourceType, resourceId)
        .ifPresent(notificationReadStateRepository::delete);
  }

  @Transactional
  public int markAllVisibleRead(String username) {
    AppUser user = loadUser(username);
    int changed = 0;
    for (AchievementReviewRequest request : listVisibleAchievementRequests(user)) {
      if (upsertReadState(user, RESOURCE_ACHIEVEMENT, request.getId())) {
        changed++;
      }
    }
    for (ProfileReviewRequest request : listVisibleProfileRequests(user)) {
      if (upsertReadState(user, RESOURCE_PROFILE, request.getId())) {
        changed++;
      }
    }
    return changed;
  }

  private boolean upsertReadState(AppUser user, String resourceType, Long resourceId) {
    NotificationReadState state = notificationReadStateRepository
        .findByUserAndResourceTypeAndResourceId(user, resourceType, resourceId)
        .orElse(null);
    if (state != null) {
      state.setReadAt(LocalDateTime.now());
      notificationReadStateRepository.save(state);
      return false;
    }

    NotificationReadState next = new NotificationReadState();
    next.setUser(user);
    next.setResourceType(resourceType);
    next.setResourceId(resourceId);
    next.setReadAt(LocalDateTime.now());
    notificationReadStateRepository.save(next);
    return true;
  }

  private void ensureVisible(AppUser user, String resourceType, Long resourceId) {
    boolean visible = RESOURCE_ACHIEVEMENT.equals(resourceType)
        ? achievementReviewRequestRepository.findById(resourceId)
            .map(request -> canSeeAchievementRequest(user, request))
            .orElse(false)
        : profileReviewRequestRepository.findById(resourceId)
            .map(request -> canSeeProfileRequest(user, request))
            .orElse(false);
    if (!visible) {
      throw new IllegalArgumentException("通知不存在或无权访问");
    }
  }

  private List<AchievementReviewRequest> listVisibleAchievementRequests(AppUser user) {
    String username = user.getUsername();
    List<AchievementReviewRequest> requests = isReviewer(user)
        ? achievementReviewRequestRepository.findAllByOrderByUpdatedAtDesc()
        : achievementReviewRequestRepository.findAllByRequester_UsernameOrderByUpdatedAtDesc(username);
    return requests.stream()
        .filter(request -> canSeeAchievementRequest(user, request))
        .toList();
  }

  private List<ProfileReviewRequest> listVisibleProfileRequests(AppUser user) {
    String username = user.getUsername();
    List<ProfileReviewRequest> requests = isReviewer(user)
        ? profileReviewRequestRepository.findAllByOrderByUpdatedAtDesc()
        : profileReviewRequestRepository.findAllByRequester_UsernameOrderByUpdatedAtDesc(username);
    return requests.stream()
        .filter(request -> canSeeProfileRequest(user, request))
        .toList();
  }

  private boolean canSeeAchievementRequest(AppUser user, AchievementReviewRequest request) {
    if ("pending".equals(request.getStatus())) {
      return canSeePendingRequest(user, request.getRequester());
    }
    return canSeeProcessedRequest(user, request.getRequester(), request.getReviewer());
  }

  private boolean canSeeProfileRequest(AppUser user, ProfileReviewRequest request) {
    if ("pending".equals(request.getStatus())) {
      return canSeePendingRequest(user, request.getRequester());
    }
    return canSeeProcessedRequest(user, request.getRequester(), request.getReviewer());
  }

  private boolean canSeePendingRequest(AppUser user, AppUser requester) {
    if (requester.getUsername().equals(user.getUsername())) {
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
    return false;
  }

  private boolean canSeeProcessedRequest(AppUser user, AppUser requester, AppUser reviewer) {
    return requester.getUsername().equals(user.getUsername())
        || (reviewer != null && reviewer.getUsername().equals(user.getUsername()));
  }

  private boolean isReviewer(AppUser user) {
    return user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.TEACHER || user.getRole() == UserRole.CADRE;
  }

  private boolean isStudentInCadreOwnClass(AppUser cadre, AppUser student) {
    String cadreClass = cadre.getClassName();
    String studentClass = student.getClassName();
    if (cadreClass == null || cadreClass.isBlank() || studentClass == null || studentClass.isBlank()) {
      return false;
    }
    return cadreClass.trim().equals(studentClass.trim());
  }

  private AppUser loadUser(String username) {
    return appUserRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
  }

  private String normalizeResourceType(String resourceType) {
    String normalized = resourceType == null ? "" : resourceType.trim().toLowerCase(Locale.ROOT);
    if (!RESOURCE_ACHIEVEMENT.equals(normalized) && !RESOURCE_PROFILE.equals(normalized)) {
      throw new IllegalArgumentException("通知类型无效");
    }
    return normalized;
  }

  private Long requireResourceId(Long resourceId) {
    if (resourceId == null || resourceId <= 0) {
      throw new IllegalArgumentException("通知标识无效");
    }
    return resourceId;
  }
}
