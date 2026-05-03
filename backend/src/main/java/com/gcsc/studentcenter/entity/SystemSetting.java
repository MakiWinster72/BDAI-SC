package com.gcsc.studentcenter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_settings")
public class SystemSetting {

  @Id
  @Column(name = "setting_key", nullable = false, length = 64)
  private String settingKey;

  @Column(name = "setting_value", nullable = false, columnDefinition = "TEXT")
  private String settingValue;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  public String getSettingKey() {
    return settingKey;
  }

  public void setSettingKey(String settingKey) {
    this.settingKey = settingKey;
  }

  public String getSettingValue() {
    return settingValue;
  }

  public void setSettingValue(String settingValue) {
    this.settingValue = settingValue;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
