package com.smart_skin.smart_skin_app_backend.controllers;

import com.smart_skin.smart_skin_app_backend.dto.*;
import com.smart_skin.smart_skin_app_backend.models.User;
import com.smart_skin.smart_skin_app_backend.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getProfile(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(
                userService.getProfile(currentUser.getId())));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @RequestBody UpdateProfileDto dto) {
        return ResponseEntity.ok(ApiResponse.ok(
                userService.updateProfile(currentUser.getId(), dto),
                "Profil mis à jour"));
    }

    @PostMapping("/me/onboarding")
    public ResponseEntity<ApiResponse<UserResponseDto>> completeOnboarding(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody OnboardingDto dto) {
        return ResponseEntity.ok(ApiResponse.ok(
                userService.completeOnboarding(currentUser.getId(), dto),
                "Onboarding complété avec succès"));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponseDto>> uploadAvatar(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok(
                userService.uploadProfileImage(currentUser.getId(), file),
                "Photo de profil mise à jour"));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal User currentUser,
            @RequestBody Map<String, String> body) {
        userService.changePassword(
                currentUser.getId(),
                body.get("oldPassword"),
                body.get("newPassword"));
        return ResponseEntity.ok(ApiResponse.ok(null, "Mot de passe modifié avec succès"));
    }

    @PutMapping("/me/fcm-token")
    public ResponseEntity<ApiResponse<Void>> updateFcmToken(
            @AuthenticationPrincipal User currentUser,
            @RequestBody Map<String, String> body) {
        userService.updateFcmToken(currentUser.getId(), body.get("fcmToken"));
        return ResponseEntity.ok(ApiResponse.ok(null, "Token FCM mis à jour"));
    }
}
