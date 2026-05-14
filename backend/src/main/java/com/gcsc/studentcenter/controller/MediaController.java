package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.UserRole;
import com.gcsc.studentcenter.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

@RestController
@RequestMapping("/api/media")
public class MediaController {

  private static final Set<String> PRIVATE_FOLDERS = Set.of("achievements", "reviews");

  private final AppUserRepository appUserRepository;
  private final Path uploadRoot;

  public MediaController(
      AppUserRepository appUserRepository,
      @Value("${app.upload-dir:./uploads}") String uploadDir) {
    this.appUserRepository = appUserRepository;
    this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
  }

  @GetMapping("/uploads/{userId}/{folder}/{filename:.+}")
  public ResponseEntity<Resource> getUpload(
      Authentication authentication,
      @PathVariable Long userId,
      @PathVariable String folder,
      @PathVariable String filename) throws MalformedURLException {
    if (!PRIVATE_FOLDERS.contains(folder)) {
      throw new IllegalArgumentException("文件类型不支持");
    }
    if (authentication == null || !canAccess(authentication.getName(), userId)) {
      return ResponseEntity.status(403).build();
    }

    Path targetFile = uploadRoot.resolve(Paths.get(String.valueOf(userId), folder, filename)).normalize();
    if (!targetFile.startsWith(uploadRoot) || !Files.isRegularFile(targetFile)) {
      return ResponseEntity.notFound().build();
    }

    Resource resource = new UrlResource(targetFile.toUri());
    String contentType = resolveContentType(targetFile);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
        .body(resource);
  }

  private boolean canAccess(String currentUsername, Long ownerId) {
    AppUser currentUser = appUserRepository.findByUsername(currentUsername)
        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    AppUser owner = appUserRepository.findById(ownerId)
        .orElseThrow(() -> new IllegalArgumentException("文件所属用户不存在"));

    if (currentUser.getId().equals(ownerId)) {
      return true;
    }
    if (currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.TEACHER) {
      return true;
    }
    return currentUser.getRole() == UserRole.CADRE
        && currentUser.getClassName() != null
        && currentUser.getClassName().equals(owner.getClassName());
  }

  private String resolveContentType(Path file) {
    try {
      String contentType = Files.probeContentType(file);
      return contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE;
    } catch (Exception ignored) {
      return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
  }
}
