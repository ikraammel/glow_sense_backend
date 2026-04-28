package com.smart_skin.smart_skin_app_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDto {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank @Email
    private String email;
    @NotBlank @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;
    private String phoneNumber;
}
