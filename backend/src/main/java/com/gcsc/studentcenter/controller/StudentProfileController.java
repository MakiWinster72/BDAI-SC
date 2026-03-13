package com.gcsc.studentcenter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gcsc.studentcenter.dto.StudentProfileRequest;
import com.gcsc.studentcenter.dto.StudentProfileResponse;
import com.gcsc.studentcenter.dto.StudentSearchResponse;
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

    @GetMapping("/{id}")
    public ResponseEntity<StudentProfileResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(studentProfileService.getProfileById(id));
    }

    @PutMapping("/me")
    public ResponseEntity<StudentProfileResponse> save(
        Authentication authentication,
        @RequestBody StudentProfileRequest request
    ) {
        return ResponseEntity.ok(studentProfileService.saveProfile(authentication.getName(), request));
    }

    @GetMapping("/search")
    public ResponseEntity<StudentSearchResponse> search(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "5") int size,
        @RequestParam(required = false) Integer classYear,
        @RequestParam(required = false) String college,
        @RequestParam(required = false) String major,
        @RequestParam(required = false) Boolean hkMoTw,
        @RequestParam(required = false) Boolean specialStudent,
        @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(
            studentProfileService.searchProfiles(
                classYear,
                college,
                major,
                hkMoTw,
                specialStudent,
                keyword,
                page,
                size
            )
        );
    }
}
