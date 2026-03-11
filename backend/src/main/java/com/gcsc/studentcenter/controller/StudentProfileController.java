package com.gcsc.studentcenter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gcsc.studentcenter.dto.StudentProfileRequest;
import com.gcsc.studentcenter.dto.StudentProfileResponse;
import com.gcsc.studentcenter.service.StudentProfileService;

@RestController
@RequestMapping("/api/student-profiles")
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    public StudentProfileController(StudentProfileService studentProfileService) {
        this.studentProfileService = studentProfileService;
    }

    @GetMapping("/me")
    public ResponseEntity<StudentProfileResponse> me(Authentication authentication) {
        return ResponseEntity.ok(studentProfileService.getProfile(authentication.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<StudentProfileResponse> save(
        Authentication authentication,
        @RequestBody StudentProfileRequest request
    ) {
        return ResponseEntity.ok(studentProfileService.saveProfile(authentication.getName(), request));
    }
}
