package com.gcsc.studentcenter.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ReviewRequestSummaryResponse {

  private final Long id;
  private final String resourceType;
  private final String status;
  private final String action;
  private final String category;
  private final String categoryLabel;
  private final Long recordId;
  private final String title;
  private final String summary;
  private final String rejectionReason;
  private final ReviewUserResponse requester;
  private final ReviewUserResponse reviewer;
  private final List<String> targetRoles;
  private final LocalDateTime createdAt;
  private final LocalDateTime updatedAt;
  private final boolean read;

  public ReviewRequestSummaryResponse(
      Long id,
      String resourceType,
      String status,
      String action,
      String category,
      String categoryLabel,
      Long recordId,
      String title,
      String summary,
      String rejectionReason,
      ReviewUserResponse requester,
      ReviewUserResponse reviewer,
      List<String> targetRoles,
      LocalDateTime createdAt,
      LocalDateTime updatedAt,
      boolean read) {
    this.id = id;
    this.resourceType = resourceType;
    this.status = status;
    this.action = action;
    this.category = category;
    this.categoryLabel = categoryLabel;
    this.recordId = recordId;
    this.title = title;
    this.summary = summary;
    this.rejectionReason = rejectionReason;
    this.requester = requester;
    this.reviewer = reviewer;
    this.targetRoles = targetRoles;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.read = read;
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

  public String getAction() {
    return action;
  }

  public String getCategory() {
    return category;
  }

  public String getCategoryLabel() {
    return categoryLabel;
  }

  public Long getRecordId() {
    return recordId;
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

  public ReviewUserResponse getRequester() {
    return requester;
  }

  public ReviewUserResponse getReviewer() {
    return reviewer;
  }

  public List<String> getTargetRoles() {
    return targetRoles;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public boolean isRead() {
    return read;
  }
}
