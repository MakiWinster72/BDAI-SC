package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.dto.UpdateUserRequest;
import com.gcsc.studentcenter.dto.UserListItemResponse;
import com.gcsc.studentcenter.service.BackupService;
import com.gcsc.studentcenter.service.JwtService;
import com.gcsc.studentcenter.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final JwtService jwtService;
    private final BackupService backupService;

    public AdminController(UserService userService, JwtService jwtService, BackupService backupService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.backupService = backupService;
    }

    private boolean isAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        try {
            Claims claims = jwtService.parseToken(authHeader.substring(7));
            String role = claims.get("role", String.class);
            return "ADMIN".equals(role);
        } catch (JwtException ex) {
            return false;
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> listUsers(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String className
    ) {
        if (!isAdmin(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.listUsersPaginated(page, size, search, role, className));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request
    ) {
        if (!isAdmin(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            var user = userService.updateUser(id, request);
            return ResponseEntity.ok(new UserListItemResponse(user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long id
    ) {
        if (!isAdmin(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/backup/db")
    public ResponseEntity<?> backupDatabase(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        if (!isAdmin(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            byte[] sqlContent = backupService.dumpDatabase();
            String filename = backupService.generateBackupFilename();
            ByteArrayResource resource = new ByteArrayResource(sqlContent);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
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
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam("file") MultipartFile file
    ) {
        if (!isAdmin(authHeader)) {
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
            return ResponseEntity.ok(Map.of("success", true, "message", "数据库恢复成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "恢复失败: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "恢复失败: " + e.getMessage()));
        }
    }
}
