package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.dto.AchievementReviewDecisionRequest;
import com.gcsc.studentcenter.dto.AchievementReviewRequestResponse;
import com.gcsc.studentcenter.dto.AchievementReviewSubmitRequest;
import com.gcsc.studentcenter.dto.SupportingDocumentsRequest;
import com.gcsc.studentcenter.service.AchievementReviewRequestService;
import com.gcsc.studentcenter.audit.AuditActions;
import com.gcsc.studentcenter.audit.AuditLogRecorder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/achievement-review-requests")
public class AchievementReviewRequestController {

  private final AchievementReviewRequestService achievementReviewRequestService;
  private final AuditLogRecorder auditLogRecorder;

  public AchievementReviewRequestController(AchievementReviewRequestService achievementReviewRequestService,
      AuditLogRecorder auditLogRecorder) {
    this.achievementReviewRequestService = achievementReviewRequestService;
    this.auditLogRecorder = auditLogRecorder;
  }

  /**
   * @deprecated Use {@code GET /api/review-inbox} for paginated inbox listing.
   */
  @Deprecated
  @GetMapping
  public ResponseEntity<List<AchievementReviewRequestResponse>> list(Authentication authentication) {
    return ResponseEntity.ok()
        .header("Deprecation", "true")
        .header("Link", "</api/review-inbox>; rel=\"successor-version\"")
        .body(achievementReviewRequestService.listVisibleRequests(authentication.getName()));
  }

  @PostMapping
  public ResponseEntity<AchievementReviewRequestResponse> submit(
      Authentication authentication,
      @RequestBody AchievementReviewSubmitRequest request) {
    return ResponseEntity.ok(
        achievementReviewRequestService.submit(authentication.getName(), request));
  }

  @PostMapping("/{id}/approve")
  public ResponseEntity<AchievementReviewRequestResponse> approve(
      Authentication authentication,
      @PathVariable("id") Long id,
      HttpServletRequest httpRequest) {
    var response = achievementReviewRequestService.approve(id, authentication.getName());
    auditLogRecorder.record(
        authentication.getName(),
        AuditActions.APPROVE_ACHIEVEMENT,
        "通过了成就审核请求 #" + id,
        httpRequest);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{id}/reject")
  public ResponseEntity<AchievementReviewRequestResponse> reject(
      Authentication authentication,
      @PathVariable("id") Long id,
      @RequestBody AchievementReviewDecisionRequest request,
      HttpServletRequest httpRequest) {
    var response = achievementReviewRequestService.reject(id, authentication.getName(), request.getReason());
    auditLogRecorder.record(
        authentication.getName(),
        AuditActions.REJECT_ACHIEVEMENT,
        "驳回了成就审核请求 #" + id + "，理由：" + request.getReason(),
        httpRequest);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> cancel(
      Authentication authentication,
      @PathVariable("id") Long id) {
    achievementReviewRequestService.cancel(id, authentication.getName());
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}/supporting-documents")
  public ResponseEntity<AchievementReviewRequestResponse> setSupportingDocuments(
      Authentication authentication,
      @PathVariable("id") Long id,
      @RequestBody SupportingDocumentsRequest body) {
    List<Map<String, String>> docs = body != null && body.getDocuments() != null
        ? body.getDocuments()
        : List.of();
    return ResponseEntity.ok(
        achievementReviewRequestService.setSupportingDocuments(id, authentication.getName(), docs));
  }
}
