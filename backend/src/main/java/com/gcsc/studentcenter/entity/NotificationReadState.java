package com.gcsc.studentcenter.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "notification_read_states",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_notification_read_state_user_resource",
            columnNames = {"user_id", "resource_type", "resource_id"})
    },
    indexes = {
        @Index(name = "idx_notification_read_state_user_type", columnList = "user_id, resource_type")
    })
public class NotificationReadState {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private AppUser user;

  @Column(name = "resource_type", nullable = false, length = 32)
  private String resourceType;

  @Column(name = "resource_id", nullable = false)
  private Long resourceId;

  @Column(name = "read_at", nullable = false)
  private LocalDateTime readAt;

  public Long getId() {
    return id;
  }

  public AppUser getUser() {
    return user;
  }

  public void setUser(AppUser user) {
    this.user = user;
  }

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

  public LocalDateTime getReadAt() {
    return readAt;
  }

  public void setReadAt(LocalDateTime readAt) {
    this.readAt = readAt;
  }
}
