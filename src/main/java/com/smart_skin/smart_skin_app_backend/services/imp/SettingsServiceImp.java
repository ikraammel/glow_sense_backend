package com.smart_skin.smart_skin_app_backend.services.imp;

import com.smart_skin.smart_skin_app_backend.dto.EmailPreferencesDto;
import com.smart_skin.smart_skin_app_backend.dto.NotificationsSettingsDto;
import com.smart_skin.smart_skin_app_backend.dto.UserResponseDto;
import com.smart_skin.smart_skin_app_backend.exception.ResourceNotFoundException;
import com.smart_skin.smart_skin_app_backend.mappers.UserMapper;
import com.smart_skin.smart_skin_app_backend.models.User;
import com.smart_skin.smart_skin_app_backend.repos.UserRepository;
import com.smart_skin.smart_skin_app_backend.services.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsServiceImp implements SettingsService {

    private final UserRepository userRepository;
    private final UserMapper     userMapper;

    @Override
    @Transactional
    public UserResponseDto updateNotificationSettings(Long userId, NotificationsSettingsDto dto) {
        User user = findUser(userId);

        if (dto.getAllNotifications() != null)
            user.setNotificationsEnabled(dto.getAllNotifications());

        if (dto.getAnalysisReminders()    != null) user.setNotifAnalysisReminders(dto.getAnalysisReminders());
        if (dto.getWeeklyReports()        != null) user.setNotifWeeklyReports(dto.getWeeklyReports());
        if (dto.getNewRecommendations()   != null) user.setNotifNewRecommendations(dto.getNewRecommendations());
        if (dto.getRoutineReminders()     != null) user.setNotifRoutineReminders(dto.getRoutineReminders());
        if (dto.getProgressUpdates()      != null) user.setNotifProgressUpdates(dto.getProgressUpdates());
        if (dto.getProductAlerts()        != null) user.setNotifProductAlerts(dto.getProductAlerts());
        if (dto.getPromotionsPush()       != null) user.setNotifPromotionsPush(dto.getPromotionsPush());
        if (dto.getReminderFrequency()    != null) user.setReminderFrequency(dto.getReminderFrequency());

        user = userRepository.save(user);
        log.info("Notification settings updated for user {}", userId);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateEmailPreferences(Long userId, EmailPreferencesDto dto) {
        User user = findUser(userId);

        if (dto.getWeeklyDigest()    != null) user.setEmailWeeklyDigest(dto.getWeeklyDigest());
        if (dto.getAnalysisResults() != null) user.setEmailAnalysisResults(dto.getAnalysisResults());
        if (dto.getSkincareTips()    != null) user.setEmailSkincareTips(dto.getSkincareTips());
        if (dto.getProductReviews()  != null) user.setEmailProductReviews(dto.getProductReviews());
        if (dto.getAccountUpdates()  != null) user.setEmailAccountUpdates(dto.getAccountUpdates());
        if (dto.getPromotions()      != null) user.setEmailPromotions(dto.getPromotions());
        if (dto.getDigestDay()       != null) user.setDigestDay(dto.getDigestDay());
        if (dto.getDigestTime()      != null) user.setDigestTime(dto.getDigestTime());

        user = userRepository.save(user);
        log.info("Email preferences updated for user {}", userId);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void requestAccountDeletion(Long userId) {
        User user = findUser(userId);
        user.setDeletionRequestedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Account deletion requested for user {} — effective in 30 days", userId);
    }

    @Override
    @Transactional
    public void cancelAccountDeletion(Long userId) {
        User user = findUser(userId);
        user.setDeletionRequestedAt(null);
        userRepository.save(user);
        log.info("Account deletion cancelled for user {}", userId);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
    }
}
