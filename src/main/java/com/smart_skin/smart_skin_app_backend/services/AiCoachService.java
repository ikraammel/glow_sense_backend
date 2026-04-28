package com.smart_skin.smart_skin_app_backend.services;

import com.smart_skin.smart_skin_app_backend.dto.CoachMessageDto;
import com.smart_skin.smart_skin_app_backend.dto.CoachRequestDto;

import java.util.List;

public interface AiCoachService {
    CoachMessageDto chat(Long userId, CoachRequestDto request);
    List<CoachMessageDto> getHistory(Long userId, String sessionId);
    void clearSession(Long userId, String sessionId);
}
