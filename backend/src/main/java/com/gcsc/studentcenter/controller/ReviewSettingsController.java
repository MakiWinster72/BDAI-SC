package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.dto.ReviewSettingsRequest;
import com.gcsc.studentcenter.dto.ReviewSettingsResponse;
import com.gcsc.studentcenter.service.ReviewSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings/review")
public class ReviewSettingsController {

  private final ReviewSettingsService reviewSettingsService;

  public ReviewSettingsController(ReviewSettingsService reviewSettingsService) {
    this.reviewSettingsService = reviewSettingsService;
  }

  @GetMapping
  public ResponseEntity<ReviewSettingsResponse> getSettings() {
    return ResponseEntity.ok(reviewSettingsService.getSettings());
  }

  @PutMapping
  public ResponseEntity<ReviewSettingsResponse> updateSettings(
      Authentication authentication,
      @RequestBody ReviewSettingsRequest request) {
    return ResponseEntity.ok(
        reviewSettingsService.updateSettings(authentication.getName(), request));
  }
}
