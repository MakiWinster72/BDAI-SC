package com.gcsc.studentcenter.dto;

public class NotificationReadStateRequest {
  private String resourceType;
  private Long resourceId;

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public Long getResourceId() {
    return resourceId;
  }

  public void setResourceId(Long resourceId) {
    this.resourceId = resourceId;
  }
}
