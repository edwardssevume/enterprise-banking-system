package com.enterprisebank.notification.controller;

import com.enterprisebank.notification.dto.NotificationResponse;
import com.enterprisebank.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>>
    getAllNotifications() {

        return ResponseEntity.ok(
                notificationService.getAllNotifications()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse>
    getNotificationById(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                notificationService.getNotificationById(
                        id,
                        authentication
                )
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>>
    getNotificationsByUser(
            @PathVariable("userId") Long userId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                notificationService
                        .getNotificationsByUser(
                                userId,
                                authentication
                        )
        );
    }

    @GetMapping("/transaction/{transactionReference}")
    public ResponseEntity<List<NotificationResponse>>
    getNotificationsByTransaction(
            @PathVariable("transactionReference")
            String transactionReference,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                notificationService
                        .getNotificationsByTransaction(
                                transactionReference,
                                authentication
                        )
        );
    }
}