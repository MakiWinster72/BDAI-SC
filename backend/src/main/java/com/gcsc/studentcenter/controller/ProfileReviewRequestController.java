package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.dto.ProfileReviewDecisionRequest;
import com.gcsc.studentcenter.dto.ProfileReviewRequestResponse;
import com.gcsc.studentcenter.dto.ProfileReviewSubmitRequest;
import com.gcsc.studentcenter.dto.SupportingDocumentsRequest;
import com.gcsc.studentcenter.service.AuditLogService;
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
  private final AuditLogService auditLogService;

  public ProfileReviewRequestController(ProfileReviewRequestService profileReviewRequestService,
      AuditLogService auditLogService) {
    this.profileReviewRequestService = profileReviewRequestService;
    this.auditLogService = auditLogService;
  }

  @GetMapping
  public ResponseEntity<List<ProfileReviewRequestResponse>> list(Authentication authentication) {
    return ResponseEntity.ok(
        profileReviewRequestService.listVisibleRequests(authentication.getName()));
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
    auditLogService.log(authentication.getName(), "APPROVE_PROFILE",
        "通过了个人信息审核请求 #" + id,
        auditLogService.resolveIpAddress(httpRequest));
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{id}/reject")
  public ResponseEntity<ProfileReviewRequestResponse> reject(
      Authentication authentication,
      @PathVariable("id") Long id,
      @RequestBody ProfileReviewDecisionRequest request,
      HttpServletRequest httpRequest) {
    var response = profileReviewRequestService.reject(id, authentication.getName(), request);
    auditLogService.log(authentication.getName(), "REJECT_PROFILE",
        "驳回了个人信息审核请求 #" + id + "，理由：" + request.getReason(),
        auditLogService.resolveIpAddress(httpRequest));
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
