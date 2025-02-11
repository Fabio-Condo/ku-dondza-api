package com.fabiocondo.controller;

import com.fabiocondo.domain.Notification;
import com.fabiocondo.exception.domain.NotificationNotFoundException;
import com.fabiocondo.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Page<Notification>> getUserNotifications(@PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId, pageable));
    }

    @PostMapping("/mark-as-read/{id}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) throws NotificationNotFoundException {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }
}

