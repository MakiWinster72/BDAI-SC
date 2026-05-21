package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.audit.AuditLogRecorder;
import com.gcsc.studentcenter.dto.AuditEventRequest;
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
@RequestMapping("/api")
public class AuditLogController {

  private final AuditLogService auditLogService;
  private final AuditLogRecorder auditLogRecorder;
  private final AppUserRepository appUserRepository;

  public AuditLogController(
      AuditLogService auditLogService,
      AuditLogRecorder auditLogRecorder,
      AppUserRepository appUserRepository) {
    this.auditLogService = auditLogService;
    this.auditLogRecorder = auditLogRecorder;
    this.appUserRepository = appUserRepository;
  }

  private boolean isAdmin(String username) {
    return appUserRepository.findByUsername(username)
        .map(user -> user.getRole() == UserRole.ADMIN)
        .orElse(false);
  }

  @GetMapping("/admin/audit-logs")
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

  /**
   * 客户端上报白名单内的审计事件（如导出），需已登录且具备对应角色。
   */
  @PostMapping("/audit-logs/events")
  public ResponseEntity<?> recordEvent(
      Authentication authentication,
      HttpServletRequest httpRequest,
      @RequestBody AuditEventRequest body) {
    String action = body.getAction();
    String detail = body.getDetail();
    if (action == null || action.isBlank() || detail == null || detail.isBlank()) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "action 和 detail 不能为空"));
    }

    AppUser user = appUserRepository.findByUsername(authentication.getName()).orElse(null);
    if (user == null || !auditLogRecorder.canRecordClientEvent(user, action.trim())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    auditLogRecorder.record(authentication.getName(), action.trim(), detail, httpRequest);
    return ResponseEntity.ok(Map.of("success", true, "message", "日志已记录"));
  }
}
