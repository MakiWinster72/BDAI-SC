package com.gcsc.studentcenter.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "achievement_review_requests")
public class AchievementReviewRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "requester_id", nullable = false)
  private AppUser requester;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "reviewer_id")
  private AppUser reviewer;

  @Column(name = "category", nullable = false, length = 32)
  private String category;

  @Column(name = "action", nullable = false, length = 16)
  private String action;

  @Column(name = "status", nullable = false, length = 16)
  private String status;

  @Column(name = "record_id")
  private Long recordId;

  @Column(name = "title", nullable = false, length = 128)
  private String title;

  @Column(name = "summary", length = 255)
  private String summary;

  @Column(name = "payload_json", nullable = false, columnDefinition = "LONGTEXT")
  private String payloadJson;

  @Column(name = "payload_snapshot_json", columnDefinition = "LONGTEXT")
  private String payloadSnapshotJson;

  @Column(name = "changes_json", columnDefinition = "LONGTEXT")
  private String changesJson;

  @Column(name = "rejection_reason", columnDefinition = "TEXT")
  private String rejectionReason;

  @Column(name = "supporting_documents_json", columnDefinition = "TEXT")
  private String supportingDocumentsJson;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  public Long getId() {
    return id;
  }

  public AppUser getRequester() {
    return requester;
  }

  public void setRequester(AppUser requester) {
    this.requester = requester;
  }

  public AppUser getReviewer() {
    return reviewer;
  }

  public void setReviewer(AppUser reviewer) {
    this.reviewer = reviewer;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Long getRecordId() {
    return recordId;
  }

  public void setRecordId(Long recordId) {
    this.recordId = recordId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getPayloadJson() {
    return payloadJson;
  }

  public void setPayloadJson(String payloadJson) {
    this.payloadJson = payloadJson;
  }

  public String getPayloadSnapshotJson() {
    return payloadSnapshotJson;
  }

  public void setPayloadSnapshotJson(String payloadSnapshotJson) {
    this.payloadSnapshotJson = payloadSnapshotJson;
  }

  public String getChangesJson() {
    return changesJson;
  }

  public void setChangesJson(String changesJson) {
    this.changesJson = changesJson;
  }

  public String getRejectionReason() {
    return rejectionReason;
  }

  public void setRejectionReason(String rejectionReason) {
    this.rejectionReason = rejectionReason;
  }

  public String getSupportingDocumentsJson() {
    return supportingDocumentsJson;
  }

  public void setSupportingDocumentsJson(String supportingDocumentsJson) {
    this.supportingDocumentsJson = supportingDocumentsJson;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
