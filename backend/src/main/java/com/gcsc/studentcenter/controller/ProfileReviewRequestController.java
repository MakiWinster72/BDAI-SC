package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.dto.ProfileReviewDecisionRequest;
import com.gcsc.studentcenter.dto.ProfileReviewRequestResponse;
import com.gcsc.studentcenter.dto.ProfileReviewSubmitRequest;
import com.gcsc.studentcenter.service.ProfileReviewRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile-review-requests")
public class ProfileReviewRequestController {

    private final ProfileReviewRequestService profileReviewRequestService;

    public ProfileReviewRequestController(ProfileReviewRequestService profileReviewRequestService) {
        this.profileReviewRequestService = profileReviewRequestService;
    }

    @GetMapping
    public ResponseEntity<List<ProfileReviewRequestResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(
            profileReviewRequestService.listVisibleRequests(authentication.getName())
        );
    }

    @PostMapping
    public ResponseEntity<ProfileReviewRequestResponse> submit(
        Authentication authentication,
        @RequestBody ProfileReviewSubmitRequest request
    ) {
        return ResponseEntity.ok(
            profileReviewRequestService.submit(authentication.getName(), request)
        );
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ProfileReviewRequestResponse> approve(
        Authentication authentication,
        @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(
            profileReviewRequestService.approve(id, authentication.getName())
        );
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ProfileReviewRequestResponse> reject(
        Authentication authentication,
        @PathVariable("id") Long id,
        @RequestBody ProfileReviewDecisionRequest request
    ) {
        return ResponseEntity.ok(
            profileReviewRequestService.reject(id, authentication.getName(), request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
        Authentication authentication,
        @PathVariable("id") Long id
    ) {
        profileReviewRequestService.cancel(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}