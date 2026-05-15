package com.gcsc.studentcenter.controller;

import com.gcsc.studentcenter.dto.NotificationReadStateRequest;
import com.gcsc.studentcenter.service.NotificationReadStateService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notification-read-states")
public class NotificationReadStateController {

  private final NotificationReadStateService notificationReadStateService;

  public NotificationReadStateController(NotificationReadStateService notificationReadStateService) {
    this.notificationReadStateService = notificationReadStateService;
  }

  @PostMapping("/read")
  public ResponseEntity<Void> markRead(
      Authentication authentication,
      @RequestBody NotificationReadStateRequest request) {
    notificationReadStateService.markRead(authentication.getName(), request);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/unread")
  public ResponseEntity<Void> markUnread(
      Authentication authentication,
      @RequestBody NotificationReadStateRequest request) {
    notificationReadStateService.markUnread(authentication.getName(), request);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/read-all")
  public ResponseEntity<Map<String, Object>> markAllRead(Authentication authentication) {
    int changed = notificationReadStateService.markAllVisibleRead(authentication.getName());
    return ResponseEntity.ok(Map.of("success", true, "changed", changed));
  }
}
