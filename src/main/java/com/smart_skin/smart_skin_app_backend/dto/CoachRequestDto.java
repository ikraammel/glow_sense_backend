package com.smart_skin.smart_skin_app_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CoachRequestDto {
    @NotBlank
    private String message;
    private String sessionId;
}
