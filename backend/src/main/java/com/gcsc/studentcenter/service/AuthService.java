package com.gcsc.studentcenter.service;

import com.gcsc.studentcenter.dto.AuthResponse;
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

import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final String USERNAME_RULE = "^[a-zA-Z0-9_]{4,32}$";

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final StudentProfileRepository studentProfileRepository;

    public AuthService(
        AppUserRepository appUserRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        StudentProfileRepository studentProfileRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.studentProfileRepository = studentProfileRepository;
    }

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
        user.setStudentNo(normalizeOptional(request.getStudentNo()));
        user.setClassName(normalizeOptional(request.getClassName()));
        user.setCollege(normalizeOptional(request.getCollege()));
        user.setCreatedAt(LocalDateTime.now());
        AppUser savedUser = appUserRepository.save(user);
        String token = jwtService.generateToken(
            savedUser.getUsername(),
            savedUser.getDisplayName(),
            savedUser.getRole().name()
        );

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
            token
        );
    }

    public AuthResponse login(LoginRequest request) {
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
            token
        );
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
            avatarUrl
        );
    }

    private String resolveAvatarUrl(AppUser user) {
        if (studentProfileRepository == null || user == null) {
            return null;
        }
        StudentProfile profile = studentProfileRepository.findByUserId(user.getId()).orElse(null);
        return profile == null ? null : profile.getAvatarUrl();
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
