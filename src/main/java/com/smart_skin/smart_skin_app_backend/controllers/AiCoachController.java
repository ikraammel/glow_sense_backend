package com.smart_skin.smart_skin_app_backend.controllers;

import com.smart_skin.smart_skin_app_backend.dto.ApiResponse;
import com.smart_skin.smart_skin_app_backend.dto.CoachMessageDto;
import com.smart_skin.smart_skin_app_backend.dto.CoachRequestDto;
import com.smart_skin.smart_skin_app_backend.models.User;
import com.smart_skin.smart_skin_app_backend.services.AiCoachService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coach")
@RequiredArgsConstructor
public class AiCoachController {

    private final AiCoachService aiCoachService;

    /**
     * POST /api/coach/chat
     * Send message to AI coach, get reply
     */
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<CoachMessageDto>> chat(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CoachRequestDto request) {
        CoachMessageDto reply = aiCoachService.chat(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(reply));
    }

    /**
     * GET /api/coach/history?sessionId=xxx
     * Retrieve conversation history for a session
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<CoachMessageDto>>> getHistory(
            @AuthenticationPrincipal User currentUser,
            @RequestParam String sessionId) {
        return ResponseEntity.ok(ApiResponse.ok(
                aiCoachService.getHistory(currentUser.getId(), sessionId)));
    }

    /**
     * DELETE /api/coach/session?sessionId=xxx
     * Clear a conversation session
     */
    @DeleteMapping("/session")
    public ResponseEntity<ApiResponse<Void>> clearSession(
            @AuthenticationPrincipal User currentUser,
            @RequestParam String sessionId) {
        aiCoachService.clearSession(currentUser.getId(), sessionId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Session effacée"));
    }
}
