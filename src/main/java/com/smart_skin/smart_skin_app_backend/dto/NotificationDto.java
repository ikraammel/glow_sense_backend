package com.smart_skin.smart_skin_app_backend.dto;

import com.smart_skin.smart_skin_app_backend.enums.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDto {
    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private boolean seen;
    private LocalDateTime createdAt;
}
