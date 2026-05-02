package com.smart_skin.smart_skin_app_backend.dto;

import com.smart_skin.smart_skin_app_backend.enums.SkinType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    private SkinType skinType;
    private String skinConcerns;
    private String ethnicity;
    private String routinePreference;
    private String effortLevel;
    private String sunExposure;
    private boolean notificationsEnabled;
    private boolean emailWeeklyDigest;
    private boolean emailAnalysisResults;
    private boolean emailSkincareTips;
    private boolean emailProductReviews;
    private boolean emailAccountUpdates;
    private boolean emailSecurityAlerts;
    private boolean emailPromotions;
    private String  digestDay;
    private String  digestTime;
    private boolean notifAnalysisReminders;
    private boolean notifWeeklyReports;
    private boolean notifNewRecommendations;
    private boolean notifRoutineReminders;
    private boolean notifProgressUpdates;
    private boolean notifProductAlerts;
    private boolean notifPromotionsPush;
    private String  reminderFrequency;
    private LocalDateTime deletionRequestedAt;
    private boolean onboardingCompleted;
    private LocalDateTime createdAt;
    private Integer age;
    private String gender;
    private String skinSensitivity;
    private String tirednessLevel;
    private String stressLevel;
    private String ingredientsToAvoid;
    private String desiredBenefits;
    private String onboardingName;
}
