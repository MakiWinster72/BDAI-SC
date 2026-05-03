package com.gcsc.studentcenter.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ProfileReviewRequestResponse {

  private final Long id;
  private final String resourceType;
  private final String status;
  private final ReviewUserResponse requester;
  private final ReviewUserResponse reviewer;
  private final List<String> targetRoles;
  private final String title;
  private final String summary;
  private final String rejectionReason;
  private final JsonNode payloadSnapshot;
  private final List<Map<String, Object>> changes;
  private final List<Map<String, Object>> supportingDocuments;
  private final LocalDateTime createdAt;
  private final LocalDateTime updatedAt;

  public ProfileReviewRequestResponse(
      Long id,
      String resourceType,
      String status,
      ReviewUserResponse requester,
      ReviewUserResponse reviewer,
      List<String> targetRoles,
      String title,
      String summary,
      String rejectionReason,
      JsonNode payloadSnapshot,
      List<Map<String, Object>> changes,
      List<Map<String, Object>> supportingDocuments,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.resourceType = resourceType;
    this.status = status;
    this.requester = requester;
    this.reviewer = reviewer;
    this.targetRoles = targetRoles;
    this.title = title;
    this.summary = summary;
    this.rejectionReason = rejectionReason;
    this.payloadSnapshot = payloadSnapshot;
    this.changes = changes;
    this.supportingDocuments = supportingDocuments;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public String getResourceType() {
    return resourceType;
  }

  public String getStatus() {
    return status;
  }

  public ReviewUserResponse getRequester() {
    return requester;
  }

  public ReviewUserResponse getReviewer() {
    return reviewer;
  }

  public List<String> getTargetRoles() {
    return targetRoles;
  }

  public String getTitle() {
    return title;
  }

  public String getSummary() {
    return summary;
  }

  public String getRejectionReason() {
    return rejectionReason;
  }

  public JsonNode getPayloadSnapshot() {
    return payloadSnapshot;
  }

  public List<Map<String, Object>> getChanges() {
    return changes;
  }

  public List<Map<String, Object>> getSupportingDocuments() {
    return supportingDocuments;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
