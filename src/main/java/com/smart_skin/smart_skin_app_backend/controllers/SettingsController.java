package com.smart_skin.smart_skin_app_backend.controllers;

import com.smart_skin.smart_skin_app_backend.dto.ApiResponse;
import com.smart_skin.smart_skin_app_backend.dto.EmailPreferencesDto;
import com.smart_skin.smart_skin_app_backend.dto.NotificationsSettingsDto;
import com.smart_skin.smart_skin_app_backend.dto.UserResponseDto;
import com.smart_skin.smart_skin_app_backend.models.User;
import com.smart_skin.smart_skin_app_backend.services.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @PutMapping("/notification-settings")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateNotificationSettings(
            @AuthenticationPrincipal User currentUser,
            @RequestBody NotificationsSettingsDto dto) {

        UserResponseDto updated = settingsService.updateNotificationSettings(currentUser.getId(), dto);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Paramètres de notifications mis à jour"));
    }

    @PutMapping("/email-preferences")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateEmailPreferences(
            @AuthenticationPrincipal User currentUser,
            @RequestBody EmailPreferencesDto dto) {

        UserResponseDto updated = settingsService.updateEmailPreferences(currentUser.getId(), dto);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Préférences email mises à jour"));
    }

    @GetMapping("/app-info")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAppInfo() {
        Map<String, String> info = Map.of(
            "appVersion",     "1.0.0",
            "buildNumber",    "42",
            "apiVersion",     "v1",
            "backendVersion", "1.0.0",
            "releaseDate",    "January 2025",
            "minAndroid",     "Android 8.0+",
            "minIos",         "iOS 14.0+"
        );
        return ResponseEntity.ok(ApiResponse.ok(info));
    }

    @PostMapping("/delete-account")
    public ResponseEntity<ApiResponse<Void>> requestAccountDeletion(
            @AuthenticationPrincipal User currentUser) {

        settingsService.requestAccountDeletion(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(null,
                "Demande de suppression enregistrée. Votre compte sera supprimé dans 30 jours."));
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<ApiResponse<Void>> cancelAccountDeletion(
            @AuthenticationPrincipal User currentUser) {

        settingsService.cancelAccountDeletion(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Suppression de compte annulée"));
    }
}
