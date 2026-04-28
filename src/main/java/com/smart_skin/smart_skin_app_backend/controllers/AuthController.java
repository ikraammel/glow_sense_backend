package com.smart_skin.smart_skin_app_backend.controllers;

import com.smart_skin.smart_skin_app_backend.dto.*;
import com.smart_skin.smart_skin_app_backend.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDto>> register(
            @Valid @RequestBody RegisterRequestDto dto) {
        AuthResponseDto result = userService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(result, "Compte créé avec succès"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody LoginRequestDto dto) {
        AuthResponseDto result = userService.login(dto);
        return ResponseEntity.ok(ApiResponse.ok(result, "Connexion réussie"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDto>> refresh(
            @RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        AuthResponseDto result = userService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.ok(result, "Token rafraîchi"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @RequestBody Map<String, String> body) {

        userService.requestPasswordReset(body.get("email"));

        return ResponseEntity.ok(
                ApiResponse.ok(null,
                        "Un email a été envoyé si le compte existe"));
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount() {
        userService.deleteCurrentUser();
        return ResponseEntity.ok(ApiResponse.ok(null, "Compte supprimé"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestBody Map<String, String> body) {
        userService.resetPassword(body.get("token"), body.get("newPassword"));
        return ResponseEntity.ok(ApiResponse.ok(null, "Mot de passe réinitialisé avec succès"));
    }
}
