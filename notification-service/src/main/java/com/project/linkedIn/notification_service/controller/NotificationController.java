package com.project.linkedIn.notification_service.controller;

import com.project.linkedIn.notification_service.auth.UserContextHolder;
import com.project.linkedIn.notification_service.entity.Notification;
import com.project.linkedIn.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/core")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping("/my")
    public ResponseEntity<List<Notification>> getMyNotifications() {
        Long userId = UserContextHolder.getCurrentUserId();
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(notifications);
    }
}
