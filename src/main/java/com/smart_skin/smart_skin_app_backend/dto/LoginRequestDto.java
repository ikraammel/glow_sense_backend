package com.smart_skin.smart_skin_app_backend.dto;

import com.smart_skin.smart_skin_app_backend.enums.SkinType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

// ==================== AUTH ====================

@Data
public class LoginRequestDto {
    @NotBlank @Email
    private String email;
    @NotBlank
    private String password;
}
