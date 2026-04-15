package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.service.JwtService;
import com.gcsc.studentcenter.service.SystemSettingsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SystemSettingsController {

    private final SystemSettingsService systemSettingsService;
    private final JwtService jwtService;

    public SystemSettingsController(SystemSettingsService systemSettingsService, JwtService jwtService) {
        this.systemSettingsService = systemSettingsService;
        this.jwtService = jwtService;
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

    private String extractUsername(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            Claims claims = jwtService.parseToken(authHeader.substring(7));
            return claims.getSubject();
        } catch (JwtException ex) {
            return null;
        }
    }

    @GetMapping("/settings/system")
    public ResponseEntity<?> getSettings(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        return ResponseEntity.ok(systemSettingsService.getSettings());
    }

    @PutMapping("/admin/settings/system")
    public ResponseEntity<?> updateSettings(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestBody Map<String, Object> updates
    ) {
        if (!isAdmin(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            String username = extractUsername(authHeader);
            return ResponseEntity.ok(systemSettingsService.updateSettings(username, updates));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
