package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.dto.AchievementUploadSettingsRequest;
import com.gcsc.studentcenter.dto.AchievementUploadSettingsResponse;
import com.gcsc.studentcenter.service.AchievementUploadSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings/achievement-upload")
public class AchievementUploadSettingsController {

  private final AchievementUploadSettingsService achievementUploadSettingsService;

  public AchievementUploadSettingsController(
      AchievementUploadSettingsService achievementUploadSettingsService) {
    this.achievementUploadSettingsService = achievementUploadSettingsService;
  }

  @GetMapping
  public ResponseEntity<AchievementUploadSettingsResponse> getSettings() {
    return ResponseEntity.ok(achievementUploadSettingsService.getSettings());
  }

  @PutMapping
  public ResponseEntity<AchievementUploadSettingsResponse> updateSettings(
      Authentication authentication,
      @RequestBody AchievementUploadSettingsRequest request) {
    return ResponseEntity.ok(
        achievementUploadSettingsService.updateSettings(authentication.getName(), request));
  }
}
