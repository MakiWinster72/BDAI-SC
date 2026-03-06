package com.gcsc.studentcenter.service;

import com.gcsc.studentcenter.dto.AuthResponse;
import com.gcsc.studentcenter.dto.LoginRequest;
import com.gcsc.studentcenter.dto.RegisterRequest;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
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

        if (appUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }

        AppUser user = new AppUser();
        user.setDisplayName(displayName);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        AppUser savedUser = appUserRepository.save(user);

        return new AuthResponse(true, "注册成功", savedUser.getUsername(), savedUser.getDisplayName());
    }

    public AuthResponse login(LoginRequest request) {
        String username = request.getUsername().trim();
        if (username.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        AppUser user = appUserRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        return new AuthResponse(true, "登录成功", user.getUsername(), user.getDisplayName());
    }
}
