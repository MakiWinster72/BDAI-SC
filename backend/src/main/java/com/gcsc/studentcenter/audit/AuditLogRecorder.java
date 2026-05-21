package com.gcsc.studentcenter.audit;

import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.UserRole;
import com.gcsc.studentcenter.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AuditLogRecorder {

  private static final int MAX_DETAIL_LENGTH = 2000;

  private static final Set<String> PRIVILEGED_EXPORT_ACTIONS = Set.of(
      AuditActions.EXPORT_STUDENTS,
      AuditActions.EXPORT_STUDENT_PDF);

  private final AuditLogService auditLogService;

  public AuditLogRecorder(AuditLogService auditLogService) {
    this.auditLogService = auditLogService;
  }

  public void record(String username, String action, String detail) {
    record(username, action, detail, (String) null);
  }

  /** 显式传入 IP（登录失败等无 HttpServletRequest 在 Service 层的场景） */
  public void record(String username, String action, String detail, String ipAddress) {
    auditLogService.log(username, action, sanitizeDetail(detail), ipAddress);
  }

  public void record(String username, String action, String detail, HttpServletRequest request) {
    String ip = request == null ? null : auditLogService.resolveIpAddress(request);
    auditLogService.log(username, action, sanitizeDetail(detail), ip);
  }

  public boolean canRecordClientEvent(AppUser user, String action) {
    if (user == null || action == null || action.isBlank()) {
      return false;
    }
    if (!PRIVILEGED_EXPORT_ACTIONS.contains(action)) {
      return false;
    }
    UserRole role = user.getRole();
    return role == UserRole.ADMIN || role == UserRole.TEACHER || role == UserRole.CADRE;
  }

  private String sanitizeDetail(String detail) {
    if (detail == null) {
      return "";
    }
    String trimmed = detail.trim();
    if (trimmed.length() <= MAX_DETAIL_LENGTH) {
      return trimmed;
    }
    return trimmed.substring(0, MAX_DETAIL_LENGTH) + "…";
  }
}
