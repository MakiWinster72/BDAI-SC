package com.gcsc.studentcenter.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.gcsc.studentcenter.dto.AchievementRecordRequest;
import com.gcsc.studentcenter.entity.AchievementContest;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.UserRole;
import com.gcsc.studentcenter.repository.AchievementCertificateRepository;
import com.gcsc.studentcenter.repository.AchievementContestRepository;
import com.gcsc.studentcenter.repository.AchievementDoubleHundredRepository;
import com.gcsc.studentcenter.repository.AchievementIeerTrainingRepository;
import com.gcsc.studentcenter.repository.AchievementJournalRepository;
import com.gcsc.studentcenter.repository.AchievementPaperRepository;
import com.gcsc.studentcenter.repository.AchievementPatentRepository;
import com.gcsc.studentcenter.repository.AchievementResearchRepository;
import com.gcsc.studentcenter.repository.AchievementSanSanXiangRepository;
import com.gcsc.studentcenter.repository.AchievementWorksRepository;
import com.gcsc.studentcenter.audit.AuditLogRecorder;
import com.gcsc.studentcenter.repository.AppUserRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AchievementServiceSecurityTest {

  @Mock
  private AppUserRepository appUserRepository;

  @Mock
  private AchievementContestRepository achievementContestRepository;

  @Mock
  private AchievementPaperRepository achievementPaperRepository;

  @Mock
  private AchievementJournalRepository achievementJournalRepository;

  @Mock
  private AchievementPatentRepository achievementPatentRepository;

  @Mock
  private AchievementCertificateRepository achievementCertificateRepository;

  @Mock
  private AchievementResearchRepository achievementResearchRepository;

  @Mock
  private AchievementWorksRepository achievementWorksRepository;

  @Mock
  private AchievementDoubleHundredRepository achievementDoubleHundredRepository;

  @Mock
  private AchievementIeerTrainingRepository achievementIeerTrainingRepository;

  @Mock
  private AchievementSanSanXiangRepository achievementSanSanXiangRepository;

  @Mock
  private AchievementUploadSettingsService achievementUploadSettingsService;

  @Mock
  private ReviewSettingsService reviewSettingsService;

  @Mock
  private AuditLogRecorder auditLogRecorder;

  private AchievementService achievementService;

  @BeforeEach
  void setUp() {
    achievementService = new AchievementService(
        appUserRepository,
        achievementContestRepository,
        achievementPaperRepository,
        achievementJournalRepository,
        achievementPatentRepository,
        achievementCertificateRepository,
        achievementResearchRepository,
        achievementWorksRepository,
        achievementDoubleHundredRepository,
        achievementIeerTrainingRepository,
        achievementSanSanXiangRepository,
        achievementUploadSettingsService,
        reviewSettingsService,
        auditLogRecorder);
  }

  @Test
  void createRejectsStudentDirectWriteWhenReviewEnabled() {
    AppUser student = user(1L, "student-a", UserRole.STUDENT);

    when(reviewSettingsService.isAchievementReviewEnabled()).thenReturn(true);
    when(appUserRepository.findByUsername("student-a")).thenReturn(Optional.of(student));

    assertThrows(
        AccessDeniedException.class,
        () -> achievementService.create("student-a", "contest", contestRequest()));
  }

  @Test
  void getByIdAllowsAdminBasedOnDatabaseRole() {
    AppUser admin = user(1L, "admin", UserRole.ADMIN);
    AppUser author = user(2L, "student-a", UserRole.STUDENT);
    AchievementContest contest = contest(9L, author);

    when(achievementContestRepository.findById(9L)).thenReturn(Optional.of(contest));
    when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

    var response = achievementService.getById("admin", "contest", 9L);

    assertEquals(9L, response.getId());
    assertEquals("contest", response.getCategory());
  }

  @Test
  void getByIdRejectsNonOwnerNonAdmin() {
    AppUser otherStudent = user(3L, "student-b", UserRole.STUDENT);
    AppUser author = user(2L, "student-a", UserRole.STUDENT);
    AchievementContest contest = contest(9L, author);

    when(achievementContestRepository.findById(9L)).thenReturn(Optional.of(contest));
    when(appUserRepository.findByUsername("student-b")).thenReturn(Optional.of(otherStudent));

    assertThrows(
        AccessDeniedException.class,
        () -> achievementService.getById("student-b", "contest", 9L));
  }

  private AchievementRecordRequest contestRequest() {
    AchievementRecordRequest request = new AchievementRecordRequest();
    request.setFields(Map.of("contestName", "测试竞赛"));
    return request;
  }

  private AchievementContest contest(Long id, AppUser author) {
    AchievementContest contest = new AchievementContest();
    ReflectionTestUtils.setField(contest, "id", id);
    contest.setAuthor(author);
    contest.setContestName("测试竞赛");
    return contest;
  }

  private AppUser user(Long id, String username, UserRole role) {
    AppUser user = new AppUser();
    ReflectionTestUtils.setField(user, "id", id);
    user.setUsername(username);
    user.setDisplayName(username);
    user.setRole(role);
    return user;
  }
}
