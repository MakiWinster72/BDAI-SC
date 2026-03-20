package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.dto.AchievementRecordRequest;
import com.gcsc.studentcenter.dto.AchievementRecordResponse;
import com.gcsc.studentcenter.service.AchievementService;
import com.gcsc.studentcenter.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

    private final AchievementService achievementService;
    private final JwtService jwtService;

    public AchievementController(AchievementService achievementService, JwtService jwtService) {
        this.achievementService = achievementService;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<List<AchievementRecordResponse>> list(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
        @RequestParam(value = "category", required = false) String category,
        @RequestParam(value = "studentNo", required = false) String studentNo,
        @RequestParam(value = "studentName", required = false) String studentName
    ) {
        String username = resolveUsername(authHeader);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(achievementService.list(username, category, studentNo, studentName));
    }

    @GetMapping("/{category}/{id}")
    public ResponseEntity<AchievementRecordResponse> getById(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
        @PathVariable("category") String category,
        @PathVariable("id") Long id
    ) {
        String username = resolveUsername(authHeader);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(achievementService.getById(username, category, id));
    }

    @PostMapping("/{category}")
    public ResponseEntity<AchievementRecordResponse> create(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
        @PathVariable("category") String category,
        @RequestBody AchievementRecordRequest request
    ) {
        String username = resolveUsername(authHeader);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(achievementService.create(username, category, request));
    }

    @PutMapping("/{category}/{id}")
    public ResponseEntity<AchievementRecordResponse> update(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
        @PathVariable("category") String category,
        @PathVariable("id") Long id,
        @RequestBody AchievementRecordRequest request
    ) {
        String username = resolveUsername(authHeader);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(achievementService.update(username, category, id, request));
    }

    @DeleteMapping("/{category}/{id}")
    public ResponseEntity<Void> delete(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
        @PathVariable("category") String category,
        @PathVariable("id") Long id
    ) {
        String username = resolveUsername(authHeader);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        achievementService.delete(username, category, id);
        return ResponseEntity.ok().build();
    }

    private String resolveUsername(String authHeader) {
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
}
