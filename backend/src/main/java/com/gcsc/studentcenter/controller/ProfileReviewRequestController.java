package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.dto.ProfileReviewDecisionRequest;
import com.gcsc.studentcenter.dto.ProfileReviewRequestResponse;
import com.gcsc.studentcenter.dto.ProfileReviewSubmitRequest;
import com.gcsc.studentcenter.dto.SupportingDocumentsRequest;
import com.gcsc.studentcenter.audit.AuditActions;
import com.gcsc.studentcenter.audit.AuditLogRecorder;
import com.gcsc.studentcenter.service.ProfileReviewRequestService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profile-review-requests")
public class ProfileReviewRequestController {

  private final ProfileReviewRequestService profileReviewRequestService;
  private final AuditLogRecorder auditLogRecorder;

  public ProfileReviewRequestController(ProfileReviewRequestService profileReviewRequestService,
      AuditLogRecorder auditLogRecorder) {
    this.profileReviewRequestService = profileReviewRequestService;
    this.auditLogRecorder = auditLogRecorder;
  }

  /**
   * @deprecated Use {@code GET /api/review-inbox} for paginated inbox listing.
   */
  @Deprecated
  @GetMapping
  public ResponseEntity<List<ProfileReviewRequestResponse>> list(Authentication authentication) {
    return ResponseEntity.ok()
        .header("Deprecation", "true")
        .header("Link", "</api/review-inbox>; rel=\"successor-version\"")
        .body(profileReviewRequestService.listVisibleRequests(authentication.getName()));
  }

  @PostMapping
  public ResponseEntity<ProfileReviewRequestResponse> submit(
      Authentication authentication,
      @RequestBody ProfileReviewSubmitRequest request) {
    return ResponseEntity.ok(
        profileReviewRequestService.submit(authentication.getName(), request));
  }

  @PostMapping("/{id}/approve")
  public ResponseEntity<ProfileReviewRequestResponse> approve(
      Authentication authentication,
      @PathVariable("id") Long id,
      HttpServletRequest httpRequest) {
    var response = profileReviewRequestService.approve(id, authentication.getName());
    auditLogRecorder.record(
        authentication.getName(),
        AuditActions.APPROVE_PROFILE,
        "通过了个人信息审核请求 #" + id,
        httpRequest);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{id}/reject")
  public ResponseEntity<ProfileReviewRequestResponse> reject(
      Authentication authentication,
      @PathVariable("id") Long id,
      @RequestBody ProfileReviewDecisionRequest request,
      HttpServletRequest httpRequest) {
    var response = profileReviewRequestService.reject(id, authentication.getName(), request);
    auditLogRecorder.record(
        authentication.getName(),
        AuditActions.REJECT_PROFILE,
        "驳回了个人信息审核请求 #" + id + "，理由：" + request.getReason(),
        httpRequest);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> cancel(
      Authentication authentication,
      @PathVariable("id") Long id) {
    profileReviewRequestService.cancel(id, authentication.getName());
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}/supporting-documents")
  public ResponseEntity<ProfileReviewRequestResponse> setSupportingDocuments(
      Authentication authentication,
      @PathVariable("id") Long id,
      @RequestBody SupportingDocumentsRequest body) {
    List<Map<String, String>> docs = body != null && body.getDocuments() != null
        ? body.getDocuments()
        : List.of();
    return ResponseEntity.ok(
        profileReviewRequestService.setSupportingDocuments(id, authentication.getName(), docs));
  }
}
