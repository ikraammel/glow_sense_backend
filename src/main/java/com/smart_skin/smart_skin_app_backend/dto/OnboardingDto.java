package com.smart_skin.smart_skin_app_backend.dto;

import com.smart_skin.smart_skin_app_backend.enums.SkinType;
import lombok.Data;

@Data
public class OnboardingDto {
    private String name;
    private SkinType skinType;
    private String skinConcerns;       // comma-separated
    private String ethnicity;
    private String routinePreference;  // minimal, moderate, complete
    private String effortLevel;        // low, medium, high
    private String sunExposure;        // rare, moderate, frequent
    private String ingredientsToAvoid; // comma-separated
    private Integer age;
    private String gender;
    private String skinSensitivity;
    private String tirednessLevel;
    private String stressLevel;
    private String desiredBenefits;
    private String onboardingName;
}
