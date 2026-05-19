package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.service.SystemSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SystemSettingsController {

  private final SystemSettingsService systemSettingsService;

  public SystemSettingsController(SystemSettingsService systemSettingsService) {
    this.systemSettingsService = systemSettingsService;
  }

  @GetMapping("/settings/system")
  public ResponseEntity<?> getSettings() {
    return ResponseEntity.ok(systemSettingsService.getSettings());
  }

  @PutMapping("/admin/settings/system")
  public ResponseEntity<?> updateSettings(
      Authentication authentication,
      @RequestBody Map<String, Object> updates) {
    return ResponseEntity.ok(systemSettingsService.updateSettings(authentication.getName(), updates));
  }
}
