package com.gcsc.studentcenter.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "username", nullable = false, length = 64)
  private String username;

  @Column(name = "action", nullable = false, length = 64)
  private String action;

  @Column(name = "detail", nullable = false, columnDefinition = "TEXT")
  private String detail;

  @Column(name = "ip_address", length = 64)
  private String ipAddress;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  public AuditLog() {
  }

  public AuditLog(String username, String action, String detail, String ipAddress, LocalDateTime createdAt) {
    this.username = username;
    this.action = action;
    this.detail = detail;
    this.ipAddress = ipAddress;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getAction() {
    return action;
  }

  public String getDetail() {
    return detail;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
