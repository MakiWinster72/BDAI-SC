package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.dto.AuthResponse;
import com.gcsc.studentcenter.dto.CaptchaResponse;
import com.gcsc.studentcenter.dto.ChangePasswordRequest;
import com.gcsc.studentcenter.dto.LoginHistoryResponse;
import com.gcsc.studentcenter.dto.LoginRequest;
import com.gcsc.studentcenter.dto.RegisterRequest;
import com.gcsc.studentcenter.dto.UserProfileResponse;
import com.gcsc.studentcenter.service.AuthService;
import com.gcsc.studentcenter.service.CaptchaService;
import com.gcsc.studentcenter.service.LoginHistoryService;
import com.gcsc.studentcenter.service.SystemSettingsService;
import com.gcsc.studentcenter.util.ClientIpResolver;
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
  private final CaptchaService captchaService;
  private final LoginHistoryService loginHistoryService;
  private final SystemSettingsService systemSettingsService;
  private final ClientIpResolver clientIpResolver;

  public AuthController(AuthService authService, CaptchaService captchaService, LoginHistoryService loginHistoryService,
      SystemSettingsService systemSettingsService, ClientIpResolver clientIpResolver) {
    this.authService = authService;
    this.captchaService = captchaService;
    this.loginHistoryService = loginHistoryService;
    this.systemSettingsService = systemSettingsService;
    this.clientIpResolver = clientIpResolver;
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
    if (!systemSettingsService.isRegistrationAllowed()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of("success", false, "message", "当前未开放注册"));
    }
    return ResponseEntity.ok(authService.register(request));
  }

  @GetMapping("/captcha")
  public ResponseEntity<CaptchaResponse> captcha() {
    return ResponseEntity.ok(captchaService.createCaptcha());
  }

  /** 登录/注册页用：仅返回是否开放注册，不含其它系统配置 */
  @GetMapping("/public-config")
  public ResponseEntity<Map<String, Object>> publicConfig() {
    return ResponseEntity.ok(Map.of(
        "allowRegistration", systemSettingsService.isRegistrationAllowed()));
  }

  /** 排查 IP 记录：看后端实际收到的地址与转发头（无需登录） */
  @GetMapping("/ip-probe")
  public ResponseEntity<Map<String, String>> ipProbe(HttpServletRequest request) {
    return ResponseEntity.ok(Map.of(
        "resolved", nullToEmpty(clientIpResolver.resolve(request)),
        "remoteAddr", nullToEmpty(request.getRemoteAddr()),
        "xForwardedFor", nullToEmpty(request.getHeader("X-Forwarded-For")),
        "xRealIp", nullToEmpty(request.getHeader("X-Real-IP"))));
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletRequest httpRequest) {
    String userAgent = httpRequest.getHeader("User-Agent");
    return ResponseEntity.ok(authService.login(request, httpRequest, userAgent));
  }

  @GetMapping("/login-history")
  public ResponseEntity<Page<LoginHistoryResponse>> getLoginHistory(
      Authentication authentication,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(
        loginHistoryService.getLoginHistory(authentication.getName(), page, size));
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

}
