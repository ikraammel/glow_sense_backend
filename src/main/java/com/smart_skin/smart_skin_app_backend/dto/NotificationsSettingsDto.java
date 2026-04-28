package com.smart_skin.smart_skin_app_backend.dto;

import lombok.Data;

@Data
public class NotificationsSettingsDto {
    private Boolean allNotifications;
    private Boolean analysisReminders;
    private Boolean weeklyReports;
    private Boolean newRecommendations;
    private Boolean routineReminders;
    private Boolean progressUpdates;
    private Boolean productAlerts;
    private Boolean promotionsPush;
    private String  reminderFrequency;
}
