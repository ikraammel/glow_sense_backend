package com.smart_skin.smart_skin_app_backend.dto;

import com.smart_skin.smart_skin_app_backend.enums.SkinType;
import lombok.Data;

@Data
public class UpdateProfileDto {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private SkinType skinType;
    private String skinConcerns;
    private String routinePreference;
    private String effortLevel;
    private String sunExposure;
    private Integer age;
    private String gender;
    private String skinSensitivity;
    private String tirednessLevel;
    private String stressLevel;
    private String ingredientsToAvoid;
    private String desiredBenefits;
    private String onboardingName;
    private boolean notificationsEnabled;
}
