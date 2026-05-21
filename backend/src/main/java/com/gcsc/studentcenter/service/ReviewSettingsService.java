package com.gcsc.studentcenter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gcsc.studentcenter.dto.ReviewSettingsRequest;
import com.gcsc.studentcenter.dto.ReviewSettingsResponse;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.SystemSetting;
import com.gcsc.studentcenter.entity.UserRole;
import com.gcsc.studentcenter.audit.AuditActions;
import com.gcsc.studentcenter.audit.AuditLogRecorder;
import com.gcsc.studentcenter.repository.AppUserRepository;
import com.gcsc.studentcenter.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ReviewSettingsService {

  public static final String SETTING_KEY = "review.settings";

  private final SystemSettingRepository systemSettingRepository;
  private final AppUserRepository appUserRepository;
  private final ObjectMapper objectMapper;
  private final AuditLogRecorder auditLogRecorder;

  public ReviewSettingsService(
      SystemSettingRepository systemSettingRepository,
      AppUserRepository appUserRepository,
      ObjectMapper objectMapper,
      AuditLogRecorder auditLogRecorder) {
    this.systemSettingRepository = systemSettingRepository;
    this.appUserRepository = appUserRepository;
    this.objectMapper = objectMapper;
    this.auditLogRecorder = auditLogRecorder;
  }

  @Transactional(readOnly = true)
  public ReviewSettingsResponse getSettings() {
    Settings settings = readSettings();
    return toResponse(settings);
  }

  @Transactional
  public ReviewSettingsResponse updateSettings(String operatorUsername, ReviewSettingsRequest request) {
    AppUser operator = appUserRepository.findByUsername(operatorUsername)
        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    if (operator.getRole() != UserRole.ADMIN) {
      throw new IllegalArgumentException("无权限修改审核设置");
    }

    Settings settings = new Settings(
        Boolean.TRUE.equals(request.getProfileReviewEnabled()),
        Boolean.TRUE.equals(request.getProfileReviewAutoApprove()),
        Boolean.TRUE.equals(request.getAchievementReviewEnabled()),
        Boolean.TRUE.equals(request.getAchievementReviewAutoApprove()));

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("profileReviewEnabled", settings.profileReviewEnabled);
    payload.put("profileReviewAutoApprove", settings.profileReviewAutoApprove);
    payload.put("achievementReviewEnabled", settings.achievementReviewEnabled);
    payload.put("achievementReviewAutoApprove", settings.achievementReviewAutoApprove);

    SystemSetting systemSetting = systemSettingRepository.findById(SETTING_KEY)
        .orElseGet(SystemSetting::new);
    systemSetting.setSettingKey(SETTING_KEY);
    systemSetting.setSettingValue(writeJson(payload));
    systemSetting.setUpdatedAt(LocalDateTime.now());
    systemSettingRepository.save(systemSetting);

    auditLogRecorder.record(operatorUsername, AuditActions.UPDATE_REVIEW_SETTINGS,
        "更新了审核设置：信息审核=" + settings.profileReviewEnabled
            + "，信息自动通过=" + settings.profileReviewAutoApprove
            + "，成就审核=" + settings.achievementReviewEnabled
            + "，成就自动通过=" + settings.achievementReviewAutoApprove);

    return toResponse(settings);
  }

  @Transactional(readOnly = true)
  public boolean isProfileReviewEnabled() {
    return readSettings().profileReviewEnabled;
  }

  @Transactional(readOnly = true)
  public boolean isProfileReviewAutoApprove() {
    Settings settings = readSettings();
    return settings.profileReviewEnabled && settings.profileReviewAutoApprove;
  }

  @Transactional(readOnly = true)
  public boolean isAchievementReviewEnabled() {
    return readSettings().achievementReviewEnabled;
  }

  @Transactional(readOnly = true)
  public boolean isAchievementReviewAutoApprove() {
    Settings settings = readSettings();
    return settings.achievementReviewEnabled && settings.achievementReviewAutoApprove;
  }

  private ReviewSettingsResponse toResponse(Settings settings) {
    return new ReviewSettingsResponse(
        settings.profileReviewEnabled,
        settings.profileReviewAutoApprove,
        settings.achievementReviewEnabled,
        settings.achievementReviewAutoApprove);
  }

  private Settings readSettings() {
    Map<String, Object> raw = systemSettingRepository.findById(SETTING_KEY)
        .map(SystemSetting::getSettingValue)
        .map(this::parseMap)
        .orElseGet(LinkedHashMap::new);

    return new Settings(
        booleanValue(raw.get("profileReviewEnabled"), true),
        booleanValue(raw.get("profileReviewAutoApprove"), false),
        booleanValue(raw.get("achievementReviewEnabled"), true),
        booleanValue(raw.get("achievementReviewAutoApprove"), false));
  }

  private Map<String, Object> parseMap(String value) {
    if (value == null || value.isBlank()) {
      return new LinkedHashMap<>();
    }
    try {
      return objectMapper.readValue(value, new TypeReference<LinkedHashMap<String, Object>>() {
      });
    } catch (JsonProcessingException exception) {
      return new LinkedHashMap<>();
    }
  }

  private String writeJson(Map<String, Object> payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("审核设置保存失败");
    }
  }

  private boolean booleanValue(Object value, boolean fallback) {
    if (value instanceof Boolean booleanValue) {
      return booleanValue;
    }
    if (value instanceof String stringValue) {
      return Boolean.parseBoolean(stringValue);
    }
    return fallback;
  }

  private record Settings(
      boolean profileReviewEnabled,
      boolean profileReviewAutoApprove,
      boolean achievementReviewEnabled,
      boolean achievementReviewAutoApprove) {
  }
}
