package com.smart_skin.smart_skin_app_backend.dto;

import lombok.Data;

@Data
public class EmailPreferencesDto {

    private Boolean weeklyDigest;
    private Boolean analysisResults;
    private Boolean skincareTips;
    private Boolean productReviews;
    private Boolean accountUpdates;
    private Boolean promotions;
    private String  digestDay;
    private String  digestTime;
}
