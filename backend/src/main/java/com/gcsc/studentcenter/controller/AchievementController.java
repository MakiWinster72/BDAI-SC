package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.dto.AchievementCreateRequest;
import com.gcsc.studentcenter.dto.AchievementResponse;
import com.gcsc.studentcenter.dto.AchievementUpdateRequest;
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
    public ResponseEntity<List<AchievementResponse>> list(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        String username = resolveUsername(authHeader);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(achievementService.list(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AchievementResponse> getById(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
        @PathVariable("id") Long id
    ) {
        String username = resolveUsername(authHeader);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(achievementService.getById(username, id));
    }

    @PostMapping
    public ResponseEntity<AchievementResponse> create(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
        @RequestBody AchievementCreateRequest request
    ) {
        String username = resolveUsername(authHeader);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(achievementService.create(username, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AchievementResponse> update(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
        @PathVariable("id") Long id,
        @RequestBody AchievementUpdateRequest request
    ) {
        String username = resolveUsername(authHeader);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(achievementService.update(username, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
        @PathVariable("id") Long id
    ) {
        String username = resolveUsername(authHeader);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        achievementService.delete(username, id);
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
