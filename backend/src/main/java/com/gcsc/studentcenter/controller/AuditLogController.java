package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.UserRole;
import com.gcsc.studentcenter.repository.AppUserRepository;
import com.gcsc.studentcenter.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class AuditLogController {

  private final AuditLogService auditLogService;
  private final AppUserRepository appUserRepository;

  public AuditLogController(AuditLogService auditLogService, AppUserRepository appUserRepository) {
    this.auditLogService = auditLogService;
    this.appUserRepository = appUserRepository;
  }

  private boolean isAdmin(String username) {
    return appUserRepository.findByUsername(username)
        .map(user -> user.getRole() == UserRole.ADMIN)
        .orElse(false);
  }

  @GetMapping
  public ResponseEntity<?> list(
      Authentication authentication,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String search) {
    if (!isAdmin(authentication.getName())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok(auditLogService.listPaginated(page, size, search));
  }

  @PostMapping
  public ResponseEntity<?> create(
      Authentication authentication,
      HttpServletRequest httpRequest,
      @RequestBody Map<String, String> body) {
    String username = authentication.getName();
    if (!isAdmin(username)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    String action = body.get("action");
    String detail = body.get("detail");
    if (action == null || action.isBlank() || detail == null || detail.isBlank()) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "action 和 detail 不能为空"));
    }
    auditLogService.log(username, action, detail, auditLogService.resolveIpAddress(httpRequest));
    return ResponseEntity.ok(Map.of("success", true, "message", "日志已记录"));
  }
}
