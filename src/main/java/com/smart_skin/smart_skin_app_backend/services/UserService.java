package com.smart_skin.smart_skin_app_backend.services;

import com.smart_skin.smart_skin_app_backend.dto.*;
import com.smart_skin.smart_skin_app_backend.models.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    AuthResponseDto register(RegisterRequestDto dto);
    AuthResponseDto login(LoginRequestDto dto);
    AuthResponseDto refreshToken(String refreshToken);
    UserResponseDto getProfile(Long userId);
    UserResponseDto updateProfile(Long userId, UpdateProfileDto dto);
    UserResponseDto completeOnboarding(Long userId, OnboardingDto dto);
    UserResponseDto uploadProfileImage(Long userId, MultipartFile file);
    void changePassword(Long userId, String oldPassword, String newPassword);
    void requestPasswordReset(String email);
    void resetPassword(String token, String newPassword);
    void updateFcmToken(Long userId, String fcmToken);
    User getCurrentUser();
    void deleteCurrentUser();
}
