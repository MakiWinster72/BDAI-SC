package com.gcsc.studentcenter.service;

import com.gcsc.studentcenter.dto.CreateUserRequest;
import com.gcsc.studentcenter.dto.UpdateUserRequest;
import com.gcsc.studentcenter.dto.UserListItemResponse;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.UserRole;
import com.gcsc.studentcenter.repository.AppUserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

  private final AppUserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final EntityManager entityManager;
  private final Path uploadRoot;

  public UserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder, EntityManager entityManager,
      @Value("${app.upload-dir:./uploads}") String uploadDir) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.entityManager = entityManager;
    this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
  }

  public AppUser createUser(CreateUserRequest request) {
    if (userRepository.existsByUsername(request.getUsername().trim())) {
      throw new RuntimeException("用户名已存在");
    }
    AppUser user = new AppUser();
    user.setUsername(request.getUsername().trim());
    user.setDisplayName(
        request.getDisplayName() != null ? request.getDisplayName().trim() : request.getUsername().trim());
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setRole(UserRole.STUDENT);
    user.setStudentNo(request.getStudentNo() != null ? request.getStudentNo().trim() : null);
    user.setClassName(request.getClassName() != null ? request.getClassName().trim() : null);
    user.setCreatedAt(LocalDateTime.now());
    return userRepository.save(user);
  }

  public Map<String, Object> listUsersPaginated(int page, int size, String search, String role, String className,
      String excludeUsername) {
    Specification<AppUser> spec = (root, query, cb) -> {
      List<Predicate> preds = new ArrayList<>();
      if (search != null && !search.isBlank()) {
        String pattern = "%" + search.trim().toLowerCase() + "%";
        preds.add(cb.or(
            cb.like(cb.lower(root.get("username")), pattern),
            cb.like(cb.lower(root.get("displayName")), pattern),
            cb.like(cb.lower(root.get("studentNo")), pattern),
            cb.like(cb.lower(root.get("className")), pattern)));
      }
      if (role != null && !role.isBlank()) {
        if (role.contains(",")) {
          String[] roles = role.split(",");
          jakarta.persistence.criteria.Path<Object> rolePath = root.get("role");
          var inClause = cb.in(rolePath);
          for (String r : roles) {
            String trimmed = r.trim();
            if (!trimmed.isEmpty()) {
              inClause.value(trimmed);
            }
          }
          preds.add(inClause);
        } else {
          preds.add(cb.equal(root.get("role"), role));
        }
      }
      if (className != null && !className.isBlank()) {
        preds.add(cb.like(cb.lower(root.get("className")), "%" + className.trim().toLowerCase() + "%"));
      }
      if (excludeUsername != null && !excludeUsername.isBlank()) {
        preds.add(cb.notEqual(root.get("username"), excludeUsername));
      }
      return cb.and(preds.toArray(new Predicate[0]));
    };

    Page<AppUser> userPage = userRepository.findAll(spec,
        PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id")));

    List<UserListItemResponse> items = userPage.getContent().stream()
        .map(UserListItemResponse::new)
        .collect(Collectors.toList());
    return Map.of(
        "data", items,
        "total", userPage.getTotalElements(),
        "page", page,
        "size", size,
        "pages", userPage.getTotalPages());
  }

  public Map<String, Object> listUsersPaginated(int page, int size, String search, String role, String className) {
    return listUsersPaginated(page, size, search, role, className, null);
  }

  public List<Long> listAllUserIds(String search, String role, String className, String excludeUsername) {
    Specification<AppUser> spec = (root, query, cb) -> {
      List<Predicate> preds = new ArrayList<>();
      if (search != null && !search.isBlank()) {
        String pattern = "%" + search.trim().toLowerCase() + "%";
        preds.add(cb.or(
            cb.like(cb.lower(root.get("username")), pattern),
            cb.like(cb.lower(root.get("displayName")), pattern),
            cb.like(cb.lower(root.get("studentNo")), pattern),
            cb.like(cb.lower(root.get("className")), pattern)));
      }
      if (role != null && !role.isBlank()) {
        if (role.contains(",")) {
          String[] roles = role.split(",");
          jakarta.persistence.criteria.Path<Object> rolePath = root.get("role");
          var inClause = cb.in(rolePath);
          for (String r : roles) {
            String trimmed = r.trim();
            if (!trimmed.isEmpty()) {
              inClause.value(trimmed);
            }
          }
          preds.add(inClause);
        } else {
          preds.add(cb.equal(root.get("role"), role));
        }
      }
      if (className != null && !className.isBlank()) {
        preds.add(cb.like(cb.lower(root.get("className")), "%" + className.trim().toLowerCase() + "%"));
      }
      if (excludeUsername != null && !excludeUsername.isBlank()) {
        preds.add(cb.notEqual(root.get("username"), excludeUsername));
      }
      return cb.and(preds.toArray(new Predicate[0]));
    };
    return userRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "id"))
        .stream()
        .map(AppUser::getId)
        .collect(Collectors.toList());
  }

  public AppUser updateUser(Long userId, UpdateUserRequest request) {
    AppUser user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("用户不存在"));

    if (request.getUsername() != null && !request.getUsername().isBlank()) {
      String newUsername = request.getUsername().trim();
      if (!newUsername.equals(user.getUsername())) {
        if (userRepository.existsByUsername(newUsername)) {
          throw new RuntimeException("用户名已存在");
        }
        user.setUsername(newUsername);
      }
    }

    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    }

    if (request.getRole() != null) {
      user.setRole(request.getRole());
    }

    if (request.getRemark() != null) {
      user.setRemark(request.getRemark().trim().isEmpty() ? null : request.getRemark().trim());
    }

    if (request.getAssignedClasses() != null
        && (user.getRole() == UserRole.TEACHER || user.getRole() == UserRole.ADMIN)) {
      user.setAssignedClasses(request.getAssignedClasses());
    }

    return userRepository.save(user);
  }

  @Transactional
  public void deleteUser(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new RuntimeException("用户不存在");
    }

    entityManager.flush();

    // Nullify reviewer_id references in review request tables
    entityManager.createNativeQuery(
        "UPDATE profile_review_requests SET reviewer_id = NULL WHERE reviewer_id = ?1")
        .setParameter(1, userId).executeUpdate();
    entityManager.createNativeQuery(
        "UPDATE achievement_review_requests SET reviewer_id = NULL WHERE reviewer_id = ?1")
        .setParameter(1, userId).executeUpdate();

    // Delete review requests where user is the requester
    entityManager.createNativeQuery(
        "DELETE FROM profile_review_requests WHERE requester_id = ?1")
        .setParameter(1, userId).executeUpdate();
    entityManager.createNativeQuery(
        "DELETE FROM achievement_review_requests WHERE requester_id = ?1")
        .setParameter(1, userId).executeUpdate();

    // Delete all achievement records
    String[] achievementTables = {
        "achievement_contests", "achievement_researches", "achievement_papers",
        "achievement_patents", "achievement_certificates", "achievement_works",
        "achievement_journals", "achievement_double_hundreds", "achievement_ieer_trainings"
    };
    for (String table : achievementTables) {
      entityManager.createNativeQuery("DELETE FROM " + table + " WHERE author_id = ?1")
          .setParameter(1, userId).executeUpdate();
    }

    // Delete login history
    entityManager.createNativeQuery("DELETE FROM login_histories WHERE user_id = ?1")
        .setParameter(1, userId).executeUpdate();

    // Delete student profile sub-tables first (education/cadre experiences)
    entityManager.createNativeQuery(
        "DELETE FROM education_experiences WHERE profile_id IN (SELECT id FROM student_profiles WHERE user_id = ?1)")
        .setParameter(1, userId).executeUpdate();
    entityManager.createNativeQuery(
        "DELETE FROM cadre_experiences WHERE profile_id IN (SELECT id FROM student_profiles WHERE user_id = ?1)")
        .setParameter(1, userId).executeUpdate();

    // Delete student profile
    entityManager.createNativeQuery("DELETE FROM student_profiles WHERE user_id = ?1")
        .setParameter(1, userId).executeUpdate();

    entityManager.flush();
    userRepository.deleteById(userId);

    // Delete user's upload folder from disk
    Path userUploadDir = uploadRoot.resolve(String.valueOf(userId));
    if (Files.exists(userUploadDir)) {
      try (var files = Files.walk(userUploadDir)) {
        files.sorted(Comparator.reverseOrder())
            .forEach(p -> {
              try {
                Files.delete(p);
              } catch (IOException ignored) {
              }
            });
      } catch (IOException ignored) {
      }
    }
  }

  public List<String> getDistinctStudentClasses() {
    return userRepository.findDistinctClassNamesByRole(UserRole.STUDENT);
  }

  public AppUser setTeacherAssignedClasses(Long userId, String assignedClasses) {
    AppUser user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("用户不存在"));
    if (user.getRole() != UserRole.TEACHER && user.getRole() != UserRole.ADMIN) {
      throw new RuntimeException("只有教师或管理员可以设置负责班级");
    }
    user.setAssignedClasses(assignedClasses);
    return userRepository.save(user);
  }

  public String getTeacherAssignedClasses(Long userId) {
    AppUser user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("用户不存在"));
    return user.getAssignedClasses();
  }

  public boolean isStudentInTeacherAssignedClass(AppUser teacher, AppUser student) {
    if (teacher.getRole() != UserRole.TEACHER) {
      return true; // non-teacher can see all
    }
    String assigned = teacher.getAssignedClasses();
    if (assigned == null || assigned.isBlank()) {
      return false; // teacher with no assigned classes sees nothing
    }
    String studentClass = student.getClassName();
    if (studentClass == null || studentClass.isBlank()) {
      return false;
    }
    String[] classes = assigned.split(",");
    for (String c : classes) {
      if (c.trim().equals(studentClass.trim())) {
        return true;
      }
    }
    return false;
  }

  public boolean isStudentInTeacherAssignedClassByClassName(AppUser teacher, String className) {
    if (teacher.getRole() != UserRole.TEACHER) {
      return true;
    }
    String assigned = teacher.getAssignedClasses();
    if (assigned == null || assigned.isBlank()) {
      return false;
    }
    if (className == null || className.isBlank()) {
      return false;
    }
    String[] classes = assigned.split(",");
    for (String c : classes) {
      if (c.trim().equals(className.trim())) {
        return true;
      }
    }
    return false;
  }
}
