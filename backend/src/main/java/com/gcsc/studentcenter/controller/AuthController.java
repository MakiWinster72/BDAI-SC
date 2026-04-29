package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.dto.AuthResponse;
import com.gcsc.studentcenter.dto.ChangePasswordRequest;
import com.gcsc.studentcenter.dto.LoginHistoryResponse;
import com.gcsc.studentcenter.dto.LoginRequest;
import com.gcsc.studentcenter.dto.RegisterRequest;
import com.gcsc.studentcenter.dto.UserProfileResponse;
import com.gcsc.studentcenter.service.AuthService;
import com.gcsc.studentcenter.service.LoginHistoryService;
import com.gcsc.studentcenter.service.SystemSettingsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final LoginHistoryService loginHistoryService;
    private final SystemSettingsService systemSettingsService;

    public AuthController(AuthService authService, LoginHistoryService loginHistoryService, SystemSettingsService systemSettingsService) {
        this.authService = authService;
        this.loginHistoryService = loginHistoryService;
        this.systemSettingsService = systemSettingsService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (!systemSettingsService.isRegistrationAllowed()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "当前未开放注册"));
        }
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = resolveIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        return ResponseEntity.ok(authService.login(request, ipAddress, userAgent));
    }

    @GetMapping("/login-history")
    public ResponseEntity<Page<LoginHistoryResponse>> getLoginHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
            loginHistoryService.getLoginHistory(authentication.getName(), page, size)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(Authentication authentication) {
        return ResponseEntity.ok(authService.getProfile(authentication.getName()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok().build();
    }

    private String resolveIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
