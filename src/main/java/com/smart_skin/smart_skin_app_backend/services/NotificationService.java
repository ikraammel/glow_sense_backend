package com.smart_skin.smart_skin_app_backend.services;

import com.smart_skin.smart_skin_app_backend.dto.NotificationDto;
import com.smart_skin.smart_skin_app_backend.models.SkinAnalysis;
import com.smart_skin.smart_skin_app_backend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    Page<NotificationDto> getNotifications(Long userId, Pageable pageable);
    long countUnread(Long userId);
    void markAllAsRead(Long userId);
    void markAsRead(Long userId, Long notificationId);
    void createAnalysisCompleteNotification(User user, SkinAnalysis analysis);
    void createReportReadyNotification(User user, String reportTitle);
    void sendWeeklyReminders();
}
