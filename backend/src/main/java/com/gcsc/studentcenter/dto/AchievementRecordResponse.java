package com.gcsc.studentcenter.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class AchievementRecordResponse {
  private Long id;
  private String category;
  private String imageUrl;
  private LocalDateTime createdAt;
  private Map<String, String> fields;

  public AchievementRecordResponse(
      Long id,
      String category,
      String imageUrl,
      LocalDateTime createdAt,
      Map<String, String> fields) {
    this.id = id;
    this.category = category;
    this.imageUrl = imageUrl;
    this.createdAt = createdAt;
    this.fields = fields;
  }

  public Long getId() {
    return id;
  }

  public String getCategory() {
    return category;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public Map<String, String> getFields() {
    return fields;
  }
}
