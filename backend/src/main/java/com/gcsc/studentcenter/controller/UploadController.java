package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.dto.UploadResponse;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.repository.AppUserRepository;
import com.gcsc.studentcenter.service.UploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

  private final UploadService uploadService;
  private final AppUserRepository appUserRepository;

  public UploadController(UploadService uploadService, AppUserRepository appUserRepository) {
    this.uploadService = uploadService;
    this.appUserRepository = appUserRepository;
  }

  @PostMapping
  public ResponseEntity<UploadResponse> upload(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "context", required = false) String context) {
    Long userId = getCurrentUserId();
    return ResponseEntity.ok(uploadService.upload(file, context, userId));
  }

  private Long getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      throw new IllegalStateException("未登录");
    }
    String username = auth.getName();
    AppUser user = appUserRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalStateException("用户不存在"));
    return user.getId();
  }
}
