package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.dto.UpdateUserRequest;
import com.gcsc.studentcenter.dto.UserListItemResponse;
import com.gcsc.studentcenter.service.JwtService;
import com.gcsc.studentcenter.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final JwtService jwtService;

    public AdminController(UserService userService, JwtService jwtService) {
        this.userService = userService;
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

    @GetMapping("/users")
    public ResponseEntity<?> listUsers(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        if (!isAdmin(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.listAllUsers());
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
}
