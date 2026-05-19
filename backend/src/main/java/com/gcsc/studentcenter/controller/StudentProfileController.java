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
  public ResponseEntity<StudentProfileResponse> getById(Authentication authentication, @PathVariable Long id) {
    return ResponseEntity.ok(studentProfileService.getProfileById(authentication.getName(), id));
  }

  @PutMapping("/me")
  public ResponseEntity<StudentProfileResponse> save(
      Authentication authentication,
      @RequestBody StudentProfileRequest request) {
    return ResponseEntity.ok(studentProfileService.saveProfile(authentication.getName(), request));
  }

  @PutMapping("/{id}")
  public ResponseEntity<StudentProfileResponse> saveById(
      Authentication authentication,
      @PathVariable Long id,
      @RequestBody StudentProfileRequest request) {
    return ResponseEntity.ok(studentProfileService.saveProfileById(authentication.getName(), id, request));
  }

  @GetMapping("/search")
  public ResponseEntity<StudentSearchResponse> search(
      Authentication authentication,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "5") int size,
      @RequestParam(required = false) Integer classYear,
      @RequestParam(required = false) String classNo,
      @RequestParam(required = false) String college,
      @RequestParam(required = false) String major,
      @RequestParam(required = false) Boolean isHk,
      @RequestParam(required = false) Boolean isMo,
      @RequestParam(required = false) Boolean isTw,
      @RequestParam(required = false) Boolean specialStudent,
      @RequestParam(required = false) String specialStudentType,
      @RequestParam(required = false) String studentCategory,
      @RequestParam(required = false) String keyword) {
    return ResponseEntity.ok(
        studentProfileService.searchProfilesForUser(
            authentication.getName(),
            classYear,
            classNo,
            college,
            major,
            isHk,
            isMo,
            isTw,
            specialStudent,
            specialStudentType,
            studentCategory,
            keyword,
            page,
            size));
  }
}
