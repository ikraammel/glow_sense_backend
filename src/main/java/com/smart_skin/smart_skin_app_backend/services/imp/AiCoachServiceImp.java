package com.smart_skin.smart_skin_app_backend.services.imp;

import com.smart_skin.smart_skin_app_backend.dto.CoachMessageDto;
import com.smart_skin.smart_skin_app_backend.dto.CoachRequestDto;
import com.smart_skin.smart_skin_app_backend.exception.ResourceNotFoundException;
import com.smart_skin.smart_skin_app_backend.models.CoachMessage;
import com.smart_skin.smart_skin_app_backend.models.SkinAnalysis;
import com.smart_skin.smart_skin_app_backend.models.User;
import com.smart_skin.smart_skin_app_backend.repos.CoachMessageRepository;
import com.smart_skin.smart_skin_app_backend.repos.SkinAnalysisRepository;
import com.smart_skin.smart_skin_app_backend.repos.UserRepository;
import com.smart_skin.smart_skin_app_backend.services.AiCoachService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiCoachServiceImp implements AiCoachService {

    private final CoachMessageRepository coachMessageRepository;
    private final UserRepository userRepository;
    private final SkinAnalysisRepository skinAnalysisRepository;

    @Value("${groq.api-key}")
    private String groqApiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String groqModel;

    @Override
    @Transactional
    public CoachMessageDto chat(Long userId, CoachRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        String sessionId = request.getSessionId() != null
                ? request.getSessionId()
                : UUID.randomUUID().toString();

        // Save user message
        CoachMessage userMsg = CoachMessage.builder()
                .user(user)
                .role("user")
                .content(request.getMessage())
                .sessionId(sessionId)
                .build();
        coachMessageRepository.save(userMsg);

        // Build conversation history for context
        List<CoachMessage> history = coachMessageRepository
                .findByUserIdAndSessionIdOrderBySentAtAsc(userId, sessionId);

        String aiReply = callOpenAI(user, history);

        // Save assistant response
        CoachMessage assistantMsg = CoachMessage.builder()
                .user(user)
                .role("assistant")
                .content(aiReply)
                .sessionId(sessionId)
                .build();
        assistantMsg = coachMessageRepository.save(assistantMsg);

        return toDto(assistantMsg);
    }

    private String callOpenAI(User user, List<CoachMessage> history) {
        String systemPrompt = buildSystemPrompt(user);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        // Add last 10 messages for context (avoid token overflow)
        int start = Math.max(0, history.size() - 10);
        for (CoachMessage msg : history.subList(start, history.size())) {
            messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> body = Map.of(
                    "model", groqModel,
                    "messages", messages,
                    "max_tokens", 300,
                    "temperature", 0.7
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.groq.com/openai/v1/chat/completions",
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            Map<?, ?> responseBody = response.getBody();
            if (responseBody != null) {
                List<?> choices = (List<?>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<?, ?> choice = (Map<?, ?>) choices.get(0);
                    Map<?, ?> msg = (Map<?, ?>) choice.get("message");
                    return (String) msg.get("content");
                }
            }
        } catch (Exception e) {
            log.error("OPENAI ERROR FULL", e);
        }

        return "Je suis désolé, je rencontre des difficultés techniques. Veuillez réessayer dans quelques instants.";
    }

    private String buildSystemPrompt(User user) {
        // Load latest analysis for context
        Optional<SkinAnalysis> latestAnalysis = skinAnalysisRepository
                .findFirstByUserIdOrderByAnalyzedAtDesc(user.getId());

        StringBuilder prompt = new StringBuilder();
        prompt.append("""
            Tu es GlowSense AI Coach, un assistant dermatologue virtuel bienveillant et expert en soins de la peau.
            Tu réponds toujours en français, avec un ton chaleureux, professionnel et encourageant.
            Tu donnes des conseils pratiques, personnalisés et basés sur les données de l'utilisateur.
            Tu ne poses jamais de diagnostic médical définitif et recommandes de consulter un dermatologue pour les cas sérieux.
            Tes réponses sont concises (3-5 phrases max) sauf si l'utilisateur demande plus de détails.
            """);

        prompt.append("\n=== PROFIL UTILISATEUR ===\n");
        prompt.append("Prénom: ").append(user.getFirstName()).append("\n");
        if (user.getSkinType() != null)
            prompt.append("Type de peau: ").append(user.getSkinType()).append("\n");
        if (user.getSkinConcerns() != null)
            prompt.append("Préoccupations: ").append(user.getSkinConcerns()).append("\n");
        if (user.getRoutinePreference() != null)
            prompt.append("Routine: ").append(user.getRoutinePreference()).append("\n");
        if (user.getSunExposure() != null)
            prompt.append("Exposition soleil: ").append(user.getSunExposure()).append("\n");
        if (user.getIngredientsToAvoid() != null)
            prompt.append("Ingrédients à éviter: ").append(user.getIngredientsToAvoid()).append("\n");

        latestAnalysis.ifPresent(a -> {
            prompt.append("\n=== DERNIÈRE ANALYSE (").append(a.getAnalyzedAt().toLocalDate()).append(") ===\n");
            prompt.append("Score global: ").append(a.getOverallScore()).append("/100\n");
            prompt.append("Hydratation: ").append(a.getHydrationScore()).append("/100\n");
            prompt.append("Acné: ").append(a.getAcneScore()).append("/100\n");
            prompt.append("Pigmentation: ").append(a.getPigmentationScore()).append("/100\n");
            if (a.getAnalysisDescription() != null)
                prompt.append("Résumé: ").append(a.getAnalysisDescription()).append("\n");
        });

        return prompt.toString();
    }

    @Override
    public List<CoachMessageDto> getHistory(Long userId, String sessionId) {
        return coachMessageRepository
                .findByUserIdAndSessionIdOrderBySentAtAsc(userId, sessionId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void clearSession(Long userId, String sessionId) {
        coachMessageRepository.deleteByUserIdAndSessionId(userId, sessionId);
    }

    private CoachMessageDto toDto(CoachMessage m) {
        CoachMessageDto dto = new CoachMessageDto();
        dto.setId(m.getId());
        dto.setRole(m.getRole());
        dto.setContent(m.getContent());
        dto.setSessionId(m.getSessionId());
        dto.setSentAt(m.getSentAt());
        return dto;
    }
}
