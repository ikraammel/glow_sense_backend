package com.smart_skin.smart_skin_app_backend.controllers;

import com.smart_skin.smart_skin_app_backend.dto.ApiResponse;
import com.smart_skin.smart_skin_app_backend.dto.NotificationDto;
import com.smart_skin.smart_skin_app_backend.models.User;
import com.smart_skin.smart_skin_app_backend.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * GET /api/notifications?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getNotifications(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.getNotifications(currentUser.getId(), PageRequest.of(page, size))));
    }

    /**
     * GET /api/notifications/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countUnread(
            @AuthenticationPrincipal User currentUser) {
        long count = notificationService.countUnread(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("count", count)));
    }

    /**
     * PUT /api/notifications/read-all
     */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal User currentUser) {
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Toutes les notifications marquées comme lues"));
    }

    /**
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        notificationService.markAsRead(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Notification lue"));
    }
}
