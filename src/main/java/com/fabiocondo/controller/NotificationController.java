package com.fabiocondo.controller;

import com.fabiocondo.domain.Notification;
import com.fabiocondo.exception.domain.NotificationNotFoundException;
import com.fabiocondo.service.impl.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;


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

    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllNotificationsAsRead(@RequestParam Long userId) {
        notificationService.markAllNotificationsAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/mark-as-read/{id}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) throws NotificationNotFoundException {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    // Nã é uma boa abordagem, depois mudar e user redis
    // A cada 10 segundos, o servidor faz uma consulta ao banco, mesmo que o usuário não receba novas notificações.
    //Solução: Usar um cache (Redis) para armazenar a contagem e atualizá-la apenas quando novas notificações chegarem.
    @GetMapping("/unread-count")
    public Flux<ServerSentEvent<Long>> getUnreadNotificationsCount(@RequestParam Long userId) {
        return Flux.interval(Duration.ofSeconds(10))

                .map(sequence -> ServerSentEvent.<Long>builder()
                        .id(String.valueOf(sequence))
                        .event("unreadNotificationsCount")
                        .data(notificationService.countUnreadNotifications(userId))
                        .build());
    }

    //@GetMapping("/unread-count")
    //public ResponseEntity<Long> getUnreadNotificationsCount(@RequestParam Long userId) {
    //    long unreadCount = notificationService.countUnreadNotifications(userId);
    //    return ResponseEntity.ok(unreadCount);
    //}
}

