package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.dto.ReviewInboxPageResponse;
import com.gcsc.studentcenter.dto.ReviewRequestSummaryResponse;
import com.gcsc.studentcenter.service.ReviewInboxService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/review-inbox")
public class ReviewInboxController {

  private final ReviewInboxService reviewInboxService;

  public ReviewInboxController(ReviewInboxService reviewInboxService) {
    this.reviewInboxService = reviewInboxService;
  }

  @GetMapping
  public ResponseEntity<ReviewInboxPageResponse> list(
      Authentication authentication,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "pending") String category,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "inbox") String scope) {
    return ResponseEntity.ok(
        reviewInboxService.listInbox(
            authentication.getName(),
            page,
            size,
            category,
            search,
            scope));
  }

  @GetMapping("/stats")
  public ResponseEntity<Map<String, Object>> stats(
      Authentication authentication,
      @RequestParam(defaultValue = "inbox") String scope) {
    return ResponseEntity.ok(reviewInboxService.getInboxStats(authentication.getName(), scope));
  }

  @GetMapping("/pending-achievement")
  public ResponseEntity<ReviewRequestSummaryResponse> pendingAchievement(
      Authentication authentication,
      @RequestParam Long recordId,
      @RequestParam String category) {
    ReviewRequestSummaryResponse response = reviewInboxService.findPendingAchievement(
        authentication.getName(),
        recordId,
        category);
    if (response == null) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{resourceType}/{id}")
  public ResponseEntity<Object> detail(
      Authentication authentication,
      @PathVariable String resourceType,
      @PathVariable Long id) {
    return ResponseEntity.ok(
        reviewInboxService.getInboxDetail(authentication.getName(), resourceType, id));
  }
}
