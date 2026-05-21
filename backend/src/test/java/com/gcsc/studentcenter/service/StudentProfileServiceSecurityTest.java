package com.gcsc.studentcenter.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gcsc.studentcenter.dto.StudentSearchItemResponse;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.StudentProfile;
import com.gcsc.studentcenter.entity.UserRole;
import com.gcsc.studentcenter.audit.AuditLogRecorder;
import com.gcsc.studentcenter.repository.AppUserRepository;
import com.gcsc.studentcenter.repository.StudentProfileRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class StudentProfileServiceSecurityTest {

  @Mock
  private StudentProfileRepository studentProfileRepository;

  @Mock
  private AppUserRepository appUserRepository;

  @Mock
  private ReviewSettingsService reviewSettingsService;

  @Mock
  private AuditLogRecorder auditLogRecorder;

  private StudentProfileService studentProfileService;

  @BeforeEach
  void setUp() {
    studentProfileService = new StudentProfileService(
        studentProfileRepository,
        appUserRepository,
        reviewSettingsService,
        auditLogRecorder);
  }

  @Test
  void getProfileByIdRejectsUnrelatedStudent() {
    AppUser operator = user(1L, "student-a", UserRole.STUDENT, "2024大数据1班");
    AppUser target = user(2L, "student-b", UserRole.STUDENT, "2024大数据2班");
    StudentProfile profile = profile(10L, target);

    when(appUserRepository.findByUsername("student-a")).thenReturn(Optional.of(operator));
    when(studentProfileRepository.findById(10L)).thenReturn(Optional.of(profile));

    assertThrows(
        AccessDeniedException.class,
        () -> studentProfileService.getProfileById("student-a", 10L));
  }

  @Test
  void searchProfilesForStudentOnlyQueriesOwnProfile() {
    AppUser operator = user(1L, "student-a", UserRole.STUDENT, "2024大数据1班");
    StudentSearchItemResponse item = new StudentSearchItemResponse(
        10L,
        "学生A",
        "S001",
        2024,
        "大数据",
        "1",
        "大数据与人工智能学院",
        false,
        false,
        false,
        false,
        null,
        null);

    when(appUserRepository.findByUsername("student-a")).thenReturn(Optional.of(operator));
    when(studentProfileRepository.searchProfileForUser(
        eq(1L),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(item)));

    var response = studentProfileService.searchProfilesForUser(
        "student-a",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        1,
        20);

    assertEquals(1, response.getTotal());
    verify(studentProfileRepository, never()).searchProfiles(
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(Pageable.class));
  }

  @Test
  void saveProfileRejectsStudentDirectWriteWhenReviewEnabled() {
    AppUser operator = user(1L, "student-a", UserRole.STUDENT, "2024大数据1班");

    when(appUserRepository.findByUsername("student-a")).thenReturn(Optional.of(operator));
    when(reviewSettingsService.isProfileReviewEnabled()).thenReturn(true);

    assertThrows(
        AccessDeniedException.class,
        () -> studentProfileService.saveProfile("student-a", new com.gcsc.studentcenter.dto.StudentProfileRequest()));
  }

  private AppUser user(Long id, String username, UserRole role, String className) {
    AppUser user = new AppUser();
    ReflectionTestUtils.setField(user, "id", id);
    user.setUsername(username);
    user.setDisplayName(username);
    user.setRole(role);
    user.setClassName(className);
    return user;
  }

  private StudentProfile profile(Long id, AppUser user) {
    StudentProfile profile = new StudentProfile();
    ReflectionTestUtils.setField(profile, "id", id);
    profile.setUser(user);
    profile.setClassName(user.getClassName());
    return profile;
  }
}
