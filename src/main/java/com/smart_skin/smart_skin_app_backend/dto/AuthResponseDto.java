package com.smart_skin.smart_skin_app_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UserResponseDto user;
}
