package com.smart_skin.smart_skin_app_backend.dto;

import com.smart_skin.smart_skin_app_backend.enums.SkinType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileDto {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private SkinType skinType;
    private String skinConcerns;
    private String routinePreference;
    private String effortLevel;
    private String sunExposure;
    private boolean notificationsEnabled;
}
