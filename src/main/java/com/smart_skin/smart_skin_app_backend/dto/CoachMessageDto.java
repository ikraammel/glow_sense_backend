package com.smart_skin.smart_skin_app_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CoachMessageDto {
    private Long id;
    private String role;
    private String content;
    private String sessionId;
    private LocalDateTime sentAt;
}
