package com.gcsc.studentcenter.service;

import com.gcsc.studentcenter.dto.AuthResponse;
import com.gcsc.studentcenter.dto.ChangePasswordRequest;
import com.gcsc.studentcenter.dto.LastLoginInfo;
import com.gcsc.studentcenter.dto.LoginRequest;
import com.gcsc.studentcenter.dto.RegisterRequest;
import com.gcsc.studentcenter.dto.UserProfileResponse;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.StudentProfile;
import com.gcsc.studentcenter.entity.UserRole;
import com.gcsc.studentcenter.repository.AppUserRepository;
import com.gcsc.studentcenter.repository.StudentProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

  private static final String USERNAME_RULE = "^[a-zA-Z0-9_]{4,32}$";
  private static final String FIXED_COLLEGE = "大数据与人工智能学院";

  private final AppUserRepository appUserRepository;
  private final StudentProfileRepository studentProfileRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final LoginHistoryService loginHistoryService;

  public AuthService(
      AppUserRepository appUserRepository,
      StudentProfileRepository studentProfileRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      LoginHistoryService loginHistoryService) {
    this.appUserRepository = appUserRepository;
    this.studentProfileRepository = studentProfileRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.loginHistoryService = loginHistoryService;
  }

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    String displayName = request.getDisplayName().trim();
    if (displayName.isEmpty()) {
      throw new IllegalArgumentException("显示名称不能为空");
    }

    String username = request.getUsername().trim();
    if (username.isEmpty()) {
      throw new IllegalArgumentException("用户名不能为空");
    }
    if (!username.matches(USERNAME_RULE)) {
      throw new IllegalArgumentException("用户名只能包含字母、数字、下划线，长度4-32位");
    }

    if (appUserRepository.existsByUsername(username)) {
      throw new IllegalArgumentException("用户名已存在");
    }

    AppUser user = new AppUser();
    UserRole role = parseRole(request.getRole());
    user.setDisplayName(displayName);
    user.setUsername(username);
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setRole(role);
    user.setStudentNo(username);
    user.setClassName(normalizeOptional(request.getClassName()));
    user.setCollege(FIXED_COLLEGE);
    user.setCreatedAt(LocalDateTime.now());
    AppUser savedUser = appUserRepository.save(user);

    StudentProfile profile = new StudentProfile();
    profile.setUser(savedUser);
    profile.setFullName(displayName);
    profile.setStudentNo(username);
    profile.setClassName(normalizeOptional(request.getClassName()));
    studentProfileRepository.save(profile);

    String token = jwtService.generateToken(
        savedUser.getUsername(),
        savedUser.getDisplayName(),
        savedUser.getRole().name());

    return new AuthResponse(
        true,
        "注册成功",
        savedUser.getUsername(),
        savedUser.getDisplayName(),
        savedUser.getRole().name(),
        savedUser.getStudentNo(),
        savedUser.getClassName(),
        savedUser.getCollege(),
        resolveAvatarUrl(savedUser),
        token,
        null);
  }

  public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
    String username = request.getUsername().trim();
    if (username.isEmpty()) {
      throw new IllegalArgumentException("用户名不能为空");
    }
    if (!username.matches(USERNAME_RULE)) {
      throw new IllegalArgumentException("用户名格式不正确");
    }

    AppUser user = appUserRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new IllegalArgumentException("用户名或密码错误");
    }
    UserRole role = roleOrDefault(user);
    String token = jwtService.generateToken(user.getUsername(), user.getDisplayName(), role.name());
    String avatarUrl = resolveAvatarUrl(user);

    LastLoginInfo lastLoginInfo = loginHistoryService.getPreviousLogin(username).orElse(null);
    loginHistoryService.recordLogin(username, ipAddress, userAgent);

    return new AuthResponse(
        true,
        "登录成功",
        user.getUsername(),
        user.getDisplayName(),
        role.name(),
        user.getStudentNo(),
        user.getClassName(),
        user.getCollege(),
        avatarUrl,
        token,
        lastLoginInfo);
  }

  public UserProfileResponse getProfile(String username) {
    AppUser user = appUserRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    UserRole role = roleOrDefault(user);
    String avatarUrl = resolveAvatarUrl(user);
    return new UserProfileResponse(
        user.getUsername(),
        user.getDisplayName(),
        role.name(),
        user.getStudentNo(),
        user.getClassName(),
        user.getCollege(),
        avatarUrl);
  }

  public void changePassword(String username, ChangePasswordRequest request) {
    AppUser user = appUserRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

    if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
      throw new IllegalArgumentException("旧密码错误");
    }

    user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    appUserRepository.save(user);
  }

  private String resolveAvatarUrl(AppUser user) {
    if (user == null) {
      return null;
    }
    return user.getAvatarUrl();
  }

  private UserRole parseRole(String rawRole) {
    if (rawRole == null || rawRole.trim().isEmpty()) {
      return UserRole.STUDENT;
    }
    try {
      return UserRole.valueOf(rawRole.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("角色只支持 STUDENT/TEACHER/ADMIN");
    }
  }

  private String normalizeOptional(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private UserRole roleOrDefault(AppUser user) {
    if (user.getRole() != null) {
      return user.getRole();
    }
    user.setRole(UserRole.STUDENT);
    appUserRepository.save(user);
    return UserRole.STUDENT;
  }
}
