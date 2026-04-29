package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.dto.AchievementReviewDecisionRequest;
import com.gcsc.studentcenter.dto.AchievementReviewRequestResponse;
import com.gcsc.studentcenter.dto.AchievementReviewSubmitRequest;
import com.gcsc.studentcenter.dto.SupportingDocumentsRequest;
import com.gcsc.studentcenter.service.AchievementReviewRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/achievement-review-requests")
public class AchievementReviewRequestController {

    private final AchievementReviewRequestService achievementReviewRequestService;

    public AchievementReviewRequestController(AchievementReviewRequestService achievementReviewRequestService) {
        this.achievementReviewRequestService = achievementReviewRequestService;
    }

    @GetMapping
    public ResponseEntity<List<AchievementReviewRequestResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(
            achievementReviewRequestService.listVisibleRequests(authentication.getName())
        );
    }

    @PostMapping
    public ResponseEntity<AchievementReviewRequestResponse> submit(
        Authentication authentication,
        @RequestBody AchievementReviewSubmitRequest request
    ) {
        return ResponseEntity.ok(
            achievementReviewRequestService.submit(authentication.getName(), request)
        );
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<AchievementReviewRequestResponse> approve(
        Authentication authentication,
        @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(
            achievementReviewRequestService.approve(id, authentication.getName())
        );
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<AchievementReviewRequestResponse> reject(
        Authentication authentication,
        @PathVariable("id") Long id,
        @RequestBody AchievementReviewDecisionRequest request
    ) {
        return ResponseEntity.ok(
            achievementReviewRequestService.reject(id, authentication.getName(), request.getReason())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
        Authentication authentication,
        @PathVariable("id") Long id
    ) {
        achievementReviewRequestService.cancel(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/supporting-documents")
    public ResponseEntity<AchievementReviewRequestResponse> setSupportingDocuments(
        Authentication authentication,
        @PathVariable("id") Long id,
        @RequestBody SupportingDocumentsRequest body
    ) {
        List<Map<String, String>> docs = body != null && body.getDocuments() != null
            ? body.getDocuments() : List.of();
        return ResponseEntity.ok(
            achievementReviewRequestService.setSupportingDocuments(id, authentication.getName(), docs)
        );
    }
}
