package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.dto.AchievementRecordRequest;
import com.gcsc.studentcenter.dto.AchievementRecordResponse;
import com.gcsc.studentcenter.service.AchievementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

  private final AchievementService achievementService;

  public AchievementController(AchievementService achievementService) {
    this.achievementService = achievementService;
  }

  @GetMapping
  public ResponseEntity<List<AchievementRecordResponse>> list(
      Authentication authentication,
      @RequestParam(value = "category", required = false) String category,
      @RequestParam(value = "studentNo", required = false) String studentNo,
      @RequestParam(value = "studentName", required = false) String studentName) {
    return ResponseEntity.ok(achievementService.list(authentication.getName(), category, studentNo, studentName));
  }

  @GetMapping("/{category}/{id}")
  public ResponseEntity<AchievementRecordResponse> getById(
      Authentication authentication,
      @PathVariable("category") String category,
      @PathVariable("id") Long id) {
    return ResponseEntity.ok(achievementService.getById(authentication.getName(), category, id));
  }

  @PostMapping("/{category}")
  public ResponseEntity<AchievementRecordResponse> create(
      Authentication authentication,
      @PathVariable("category") String category,
      @RequestBody AchievementRecordRequest request) {
    return ResponseEntity.ok(achievementService.create(authentication.getName(), category, request));
  }

  @PutMapping("/{category}/{id}")
  public ResponseEntity<AchievementRecordResponse> update(
      Authentication authentication,
      @PathVariable("category") String category,
      @PathVariable("id") Long id,
      @RequestBody AchievementRecordRequest request) {
    return ResponseEntity.ok(achievementService.update(authentication.getName(), category, id, request));
  }

  @DeleteMapping("/{category}/{id}")
  public ResponseEntity<Void> delete(
      Authentication authentication,
      @PathVariable("category") String category,
      @PathVariable("id") Long id) {
    achievementService.delete(authentication.getName(), category, id);
    return ResponseEntity.ok().build();
  }
}
