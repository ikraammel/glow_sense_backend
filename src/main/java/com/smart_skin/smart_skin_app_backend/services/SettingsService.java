package com.smart_skin.smart_skin_app_backend.services;

import com.smart_skin.smart_skin_app_backend.dto.EmailPreferencesDto;
import com.smart_skin.smart_skin_app_backend.dto.NotificationsSettingsDto;
import com.smart_skin.smart_skin_app_backend.dto.UserResponseDto;

public interface SettingsService {
    UserResponseDto updateNotificationSettings(Long userId, NotificationsSettingsDto dto);
    UserResponseDto updateEmailPreferences(Long userId, EmailPreferencesDto dto);
    void requestAccountDeletion(Long userId);
    void cancelAccountDeletion(Long userId);
}
