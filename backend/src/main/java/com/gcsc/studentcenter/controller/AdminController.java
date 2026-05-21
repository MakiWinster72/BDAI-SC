package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.dto.CreateUserRequest;
import com.gcsc.studentcenter.dto.UpdateUserRequest;
import com.gcsc.studentcenter.dto.UserListItemResponse;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.UserRole;
import com.gcsc.studentcenter.repository.AppUserRepository;
import com.gcsc.studentcenter.audit.AuditActions;
import com.gcsc.studentcenter.audit.AuditLogRecorder;
import com.gcsc.studentcenter.service.BackupService;
import com.gcsc.studentcenter.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

  private final UserService userService;
  private final BackupService backupService;
  private final AppUserRepository appUserRepository;
  private final AuditLogRecorder auditLogRecorder;

  @Value("${app.upload-dir:./uploads}")
  private String uploadDir;

  public AdminController(UserService userService, BackupService backupService,
      AppUserRepository appUserRepository, AuditLogRecorder auditLogRecorder) {
    this.userService = userService;
    this.backupService = backupService;
    this.appUserRepository = appUserRepository;
    this.auditLogRecorder = auditLogRecorder;
  }

  private boolean isAdmin(Authentication authentication) {
    if (authentication == null || authentication.getName() == null) {
      return false;
    }
    return appUserRepository.findByUsername(authentication.getName())
        .map(user -> user.getRole() == UserRole.ADMIN)
        .orElse(false);
  }

  @PostMapping("/users")
  public ResponseEntity<?> createUser(
      Authentication authentication,
      HttpServletRequest httpRequest,
      @RequestBody CreateUserRequest request) {
    if (!isAdmin(authentication)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    try {
      var user = userService.createUser(request);
      auditLogRecorder.record(
          authentication.getName(),
          AuditActions.CREATE_USER,
          "创建了用户 " + user.getUsername() + "（" + user.getDisplayName() + "）",
          httpRequest);
      return ResponseEntity.ok(new UserListItemResponse(user));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    }
  }

  @GetMapping("/users")
  public ResponseEntity<?> listUsers(
      Authentication authentication,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String role,
      @RequestParam(required = false) String className) {
    if (!isAdmin(authentication)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    String currentUsername = authentication.getName();
    return ResponseEntity.ok(userService.listUsersPaginated(page, size, search, role, className, currentUsername));
  }

  @GetMapping("/users/ids")
  public ResponseEntity<?> listAllUserIds(
      Authentication authentication,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String role,
      @RequestParam(required = false) String className) {
    if (!isAdmin(authentication)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    String currentUsername = authentication.getName();
    return ResponseEntity.ok(userService.listAllUserIds(search, role, className, currentUsername));
  }

  @PutMapping("/users/{id}")
  public ResponseEntity<?> updateUser(
      Authentication authentication,
      HttpServletRequest httpRequest,
      @PathVariable Long id,
      @RequestBody UpdateUserRequest request) {
    if (!isAdmin(authentication)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    try {
      var user = userService.updateUser(id, request);
      auditLogRecorder.record(
          authentication.getName(),
          AuditActions.UPDATE_USER,
          "更新了用户 #" + id + "（" + user.getUsername() + "，角色 " + user.getRole() + "）",
          httpRequest);
      return ResponseEntity.ok(Map.of("success", true, "data", new UserListItemResponse(user)));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    }
  }

  @DeleteMapping("/users/{id}")
  public ResponseEntity<?> deleteUser(
      Authentication authentication,
      HttpServletRequest httpRequest,
      @PathVariable Long id) {
    if (!isAdmin(authentication)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    try {
      String deletedUsername = appUserRepository.findById(id)
          .map(AppUser::getUsername)
          .orElse("id=" + id);
      userService.deleteUser(id);
      auditLogRecorder.record(
          authentication.getName(),
          AuditActions.DELETE_USER,
          "删除了用户 #" + id + "（" + deletedUsername + "）",
          httpRequest);
      return ResponseEntity.ok(Map.of("success", true));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    }
  }

  @GetMapping("/classes")
  public ResponseEntity<?> listClasses(
      Authentication authentication) {
    if (!isAdmin(authentication)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    List<String> classes = userService.getDistinctStudentClasses();
    return ResponseEntity.ok(Map.of("data", classes));
  }

  @PutMapping("/teachers/{id}/assigned-classes")
  public ResponseEntity<?> updateTeacherAssignedClasses(
      Authentication authentication,
      HttpServletRequest httpRequest,
      @PathVariable Long id,
      @RequestBody Map<String, String> request) {
    if (!isAdmin(authentication)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    try {
      String assignedClasses = request.get("assignedClasses");
      var user = userService.setTeacherAssignedClasses(id, assignedClasses);
      auditLogRecorder.record(
          authentication.getName(),
          AuditActions.UPDATE_TEACHER_CLASSES,
          "更新了教师 #" + id + "（" + user.getUsername() + "）负责班级",
          httpRequest);
      return ResponseEntity.ok(Map.of("success", true, "assignedClasses", user.getAssignedClasses()));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    }
  }

  @GetMapping("/teachers/{id}/assigned-classes")
  public ResponseEntity<?> getTeacherAssignedClasses(
      Authentication authentication,
      @PathVariable Long id) {
    if (!isAdmin(authentication)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    try {
      String assignedClasses = userService.getTeacherAssignedClasses(id);
      return ResponseEntity.ok(Map.of("assignedClasses", assignedClasses));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    }
  }

  @GetMapping("/backup/db")
  public ResponseEntity<?> backupDatabase(
      Authentication authentication,
      HttpServletRequest httpRequest) {
    String username = authentication.getName();
    if (!isAdmin(authentication)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    try {
      byte[] sqlContent = backupService.dumpDatabase();
      auditLogRecorder.record(username, AuditActions.BACKUP_DB, "导出数据库备份", httpRequest);
      ByteArrayResource resource = new ByteArrayResource(sqlContent);
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .header(HttpHeaders.CONTENT_DISPOSITION,
              "attachment; filename=\"" + backupService.generateBackupFilename() + "\"")
          .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(sqlContent.length))
          .body(resource);
    } catch (RuntimeException e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("success", false, "message", "备份失败: " + e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("success", false, "message", "备份失败: " + e.getMessage()));
    }
  }

  @PostMapping("/restore/db")
  public ResponseEntity<?> restoreDatabase(
      Authentication authentication,
      @RequestParam("file") MultipartFile file,
      HttpServletRequest httpRequest) {
    String username = authentication.getName();
    if (!isAdmin(authentication)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    if (file.isEmpty()) {
      return ResponseEntity.badRequest()
          .body(Map.of("success", false, "message", "请上传 SQL 备份文件"));
    }
    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".sql")) {
      return ResponseEntity.badRequest()
          .body(Map.of("success", false, "message", "请上传 .sql 格式的备份文件"));
    }
    try {
      byte[] sqlContent = file.getBytes();
      backupService.restoreDatabase(sqlContent);
      auditLogRecorder.record(username, AuditActions.RESTORE_DB, "恢复数据库备份", httpRequest);
      return ResponseEntity.ok(Map.of("success", true, "message", "数据库恢复成功"));
    } catch (RuntimeException e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("success", false, "message", "恢复失败: " + e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("success", false, "message", "恢复失败: " + e.getMessage()));
    }
  }

  @GetMapping("/backup/attachments")
  public ResponseEntity<?> backupAttachments(
      Authentication authentication,
      HttpServletRequest httpRequest) {
    String username = authentication.getName();
    if (!isAdmin(authentication)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    try {
      byte[] zipContent = backupService.dumpAttachments();
      if (zipContent.length == 0) {
        return ResponseEntity.ok(Map.of("success", false, "message", "附件目录为空，无需导出"));
      }
      auditLogRecorder.record(username, AuditActions.BACKUP_ATTACHMENTS, "导出附件备份", httpRequest);
      ByteArrayResource resource = new ByteArrayResource(zipContent);
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .header(HttpHeaders.CONTENT_DISPOSITION,
              "attachment; filename=\"" + backupService.generateAttachmentsFilename() + "\"")
          .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(zipContent.length))
          .body(resource);
    } catch (RuntimeException e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("success", false, "message", "导出失败: " + e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("success", false, "message", "导出失败: " + e.getMessage()));
    }
  }

  @PostMapping("/restore/attachments")
  public ResponseEntity<?> restoreAttachments(
      Authentication authentication,
      @RequestParam("file") MultipartFile file,
      HttpServletRequest httpRequest) {
    String username = authentication.getName();
    if (!isAdmin(authentication)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    if (file.isEmpty()) {
      return ResponseEntity.badRequest()
          .body(Map.of("success", false, "message", "请上传附件压缩包"));
    }
    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
      return ResponseEntity.badRequest()
          .body(Map.of("success", false, "message", "请上传 .zip 格式的压缩文件"));
    }
    try {
      byte[] zipContent = file.getBytes();
      backupService.restoreAttachments(zipContent);
      auditLogRecorder.record(username, AuditActions.RESTORE_ATTACHMENTS, "恢复附件备份", httpRequest);
      return ResponseEntity.ok(Map.of("success", true, "message", "附件恢复成功"));
    } catch (RuntimeException e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("success", false, "message", "恢复失败: " + e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("success", false, "message", "恢复失败: " + e.getMessage()));
    }
  }

  @GetMapping("/storage-analysis")
  public ResponseEntity<?> storageAnalysis(
      Authentication authentication) {
    if (!isAdmin(authentication)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    try {
      Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
      List<Map<String, Object>> entries = new ArrayList<>();
      long totalBytes = 0;

      try (DirectoryStream<Path> stream = Files.newDirectoryStream(uploadRoot)) {
        for (Path entry : stream) {
          if (!Files.isDirectory(entry))
            continue;
          String name = entry.getFileName().toString();
          if (!name.matches("\\d+"))
            continue;

          long userId = Long.parseLong(name);
          long size = folderSize(entry);
          totalBytes += size;

          var userOpt = appUserRepository.findById(userId);
          String username = userOpt.map(AppUser::getUsername).orElse("(已删除)");
          String displayName = userOpt.map(AppUser::getDisplayName).orElse("");
          boolean userExists = userOpt.isPresent();

          Map<String, Object> item = new LinkedHashMap<>();
          item.put("userId", userId);
          item.put("folderName", name);
          item.put("username", username);
          item.put("displayName", displayName);
          item.put("userExists", userExists);
          item.put("sizeBytes", size);
          item.put("sizeFormatted", formatSize(size));
          entries.add(item);
        }
      }

      entries.sort((a, b) -> Long.compare((Long) b.get("sizeBytes"), (Long) a.get("sizeBytes")));
      List<Map<String, Object>> top30 = entries.size() > 30 ? entries.subList(0, 30) : entries;

      return ResponseEntity.ok(Map.of(
          "entries", top30,
          "totalBytes", totalBytes,
          "totalFormatted", formatSize(totalBytes),
          "totalUsers", entries.size()));
    } catch (IOException e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("success", false, "message", "扫描失败: " + e.getMessage()));
    }
  }

  @DeleteMapping("/storage/{userId}")
  public ResponseEntity<?> deleteStorage(
      Authentication authentication,
      @PathVariable Long userId,
      HttpServletRequest httpRequest) {
    String username = authentication.getName();
    if (!isAdmin(authentication)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    try {
      Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
      Path userDir = uploadRoot.resolve(String.valueOf(userId)).normalize();
      if (!userDir.startsWith(uploadRoot)) {
        return ResponseEntity.badRequest()
            .body(Map.of("success", false, "message", "非法路径"));
      }
      if (!Files.exists(userDir) || !Files.isDirectory(userDir)) {
        return ResponseEntity.badRequest()
            .body(Map.of("success", false, "message", "文件夹不存在"));
      }
      long size = folderSize(userDir);
      deleteFolder(userDir);
      auditLogRecorder.record(
          username,
          AuditActions.DELETE_STORAGE,
          "删除用户 #" + userId + " 的附件文件夹 (" + formatSize(size) + ")",
          httpRequest);
      return ResponseEntity.ok(Map.of(
          "success", true,
          "message", "已删除 " + formatSize(size) + " 的附件"));
    } catch (IOException e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("success", false, "message", "删除失败: " + e.getMessage()));
    }
  }

  private long folderSize(Path folder) throws IOException {
    try (var files = Files.walk(folder)) {
      return files
          .filter(Files::isRegularFile)
          .mapToLong(p -> {
            try {
              return Files.size(p);
            } catch (IOException e) {
              return 0L;
            }
          })
          .sum();
    }
  }

  private void deleteFolder(Path folder) throws IOException {
    if (!Files.exists(folder)) return;
    try (var files = Files.walk(folder)) {
      files
          .sorted(java.util.Comparator.reverseOrder())
          .forEach(p -> {
            try {
              Files.delete(p);
            } catch (IOException ignored) {
            }
          });
    }
  }

  private String formatSize(long bytes) {
    if (bytes < 1024)
      return bytes + " B";
    if (bytes < 1024 * 1024)
      return String.format("%.1f KB", bytes / 1024.0);
    if (bytes < 1024 * 1024 * 1024)
      return String.format("%.1f MB", bytes / (1024.0 * 1024));
    return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
  }
}
