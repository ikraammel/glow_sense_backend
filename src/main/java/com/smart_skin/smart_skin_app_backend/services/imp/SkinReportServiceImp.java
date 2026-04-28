package com.smart_skin.smart_skin_app_backend.services.imp;

import com.smart_skin.smart_skin_app_backend.dto.SkinReportDto;
import com.smart_skin.smart_skin_app_backend.exception.BadRequestException;
import com.smart_skin.smart_skin_app_backend.exception.ResourceNotFoundException;
import com.smart_skin.smart_skin_app_backend.models.SkinAnalysis;
import com.smart_skin.smart_skin_app_backend.models.SkinReport;
import com.smart_skin.smart_skin_app_backend.models.User;
import com.smart_skin.smart_skin_app_backend.repos.SkinAnalysisRepository;
import com.smart_skin.smart_skin_app_backend.repos.SkinReportRepository;
import com.smart_skin.smart_skin_app_backend.repos.UserRepository;
import com.smart_skin.smart_skin_app_backend.services.NotificationService;
import com.smart_skin.smart_skin_app_backend.services.SkinReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkinReportServiceImp implements SkinReportService {

    private final SkinReportRepository skinReportRepository;
    private final SkinAnalysisRepository skinAnalysisRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Value("${groq.api-key}")
    private String groqApiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String groqModel;

    @Override
    @Transactional
    public SkinReportDto generateReport(Long userId, LocalDate from, LocalDate to) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        List<SkinAnalysis> analyses = skinAnalysisRepository
                .findByUserIdAndAnalyzedAtBetweenOrderByAnalyzedAtAsc(userId, start, end);

        if (analyses.isEmpty()) {
            throw new BadRequestException("Aucune analyse disponible pour cette période");
        }

        // Compute averages
        OptionalDouble avgOverall     = analyses.stream().filter(a -> a.getOverallScore() != null)
                .mapToInt(SkinAnalysis::getOverallScore).average();
        OptionalDouble avgHydration   = analyses.stream().filter(a -> a.getHydrationScore() != null)
                .mapToInt(SkinAnalysis::getHydrationScore).average();
        OptionalDouble avgAcne        = analyses.stream().filter(a -> a.getAcneScore() != null)
                .mapToInt(SkinAnalysis::getAcneScore).average();
        OptionalDouble avgPigmentation = analyses.stream().filter(a -> a.getPigmentationScore() != null)
                .mapToInt(SkinAnalysis::getPigmentationScore).average();

        // Generate AI summary
        String summary = generateAiSummary(user, analyses,
                avgOverall.orElse(0), avgHydration.orElse(0),
                avgAcne.orElse(0), avgPigmentation.orElse(0));

        String progress = generateProgressNotes(analyses);
        String title = "Rapport du " + from + " au " + to;

        SkinReport report = SkinReport.builder()
                .user(user)
                .title(title)
                .periodStart(from)
                .periodEnd(to)
                .totalAnalyses(analyses.size())
                .averageOverallScore(avgOverall.isPresent() ? Math.round(avgOverall.getAsDouble() * 10.0) / 10.0 : null)
                .averageHydrationScore(avgHydration.isPresent() ? Math.round(avgHydration.getAsDouble() * 10.0) / 10.0 : null)
                .averageAcneScore(avgAcne.isPresent() ? Math.round(avgAcne.getAsDouble() * 10.0) / 10.0 : null)
                .averagePigmentationScore(avgPigmentation.isPresent() ? Math.round(avgPigmentation.getAsDouble() * 10.0) / 10.0 : null)
                .globalSummary(summary)
                .progressNotes(progress)
                .includedAnalyses(analyses)
                .build();

        report = skinReportRepository.save(report);
        notificationService.createReportReadyNotification(user, title);

        log.info("Report generated for user {} — {} analyses from {} to {}", userId, analyses.size(), from, to);
        return toDto(report);
    }

    private String generateAiSummary(User user, List<SkinAnalysis> analyses,
                                      double avgOverall, double avgHydration,
                                      double avgAcne, double avgPigmentation) {
        String prompt = """
            Tu es un dermatologue IA. Génère un résumé bienveillant et professionnel en français (4-6 phrases) 
            pour un rapport de suivi cutané avec ces données:
            - Utilisateur: %s, type de peau: %s
            - Nombre d'analyses: %d
            - Score moyen global: %.1f/100
            - Hydratation moyenne: %.1f/100
            - Acné moyenne: %.1f/100 (100 = pas d'acné)
            - Pigmentation moyenne: %.1f/100
            
            Sois encourageant, précis et donne 2 conseils prioritaires pour la période suivante.
            """.formatted(
                user.getFirstName(),
                user.getSkinType() != null ? user.getSkinType().name() : "inconnu",
                analyses.size(), avgOverall, avgHydration, avgAcne, avgPigmentation
        );

        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> body = Map.of(
                    "model", groqModel,
                    "messages", List.of(Map.of("role", "user", "content", prompt)),
                    "max_tokens", 400
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.groq.com/openai/v1/chat/completions",
                    HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

            Map<?, ?> rb = response.getBody();
            if (rb != null) {
                List<?> choices = (List<?>) rb.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    return (String) ((Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message")).get("content");
                }
            }
        } catch (Exception e) {
            log.warn("AI summary generation failed: {}", e.getMessage());
        }

        return String.format(
                "Votre bilan cutané pour cette période montre un score global moyen de %.0f/100. " +
                "Votre hydratation se maintient à %.0f/100 et votre indice d'acné est de %.0f/100. " +
                "Continuez votre routine de soins et consultez les recommandations personnalisées de chaque analyse.",
                avgOverall, avgHydration, avgAcne);
    }

    private String generateProgressNotes(List<SkinAnalysis> analyses) {
        if (analyses.size() < 2) return "Continuez vos analyses régulières pour suivre votre progression.";

        SkinAnalysis first = analyses.get(0);
        SkinAnalysis last  = analyses.get(analyses.size() - 1);

        int scoreDiff = (last.getOverallScore() != null && first.getOverallScore() != null)
                ? last.getOverallScore() - first.getOverallScore() : 0;

        if (scoreDiff > 5)  return String.format("📈 Progression positive : +%d points sur le score global. Continuez ainsi !", scoreDiff);
        if (scoreDiff < -5) return String.format("📉 Score en légère baisse : %d points. Consultez les recommandations pour ajuster votre routine.", scoreDiff);
        return "📊 Score stable sur la période. Maintenez votre routine actuelle.";
    }

    @Override
    public Page<SkinReportDto> getReports(Long userId, Pageable pageable) {
        return skinReportRepository.findByUserIdOrderByGeneratedAtDesc(userId, pageable).map(this::toDto);
    }

    @Override
    public SkinReportDto getReport(Long userId, Long reportId) {
        SkinReport report = skinReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Rapport non trouvé"));
        if (!report.getUser().getId().equals(userId)) throw new BadRequestException("Accès non autorisé");
        return toDto(report);
    }

    private SkinReportDto toDto(SkinReport r) {
        SkinReportDto dto = new SkinReportDto();
        dto.setId(r.getId());
        dto.setTitle(r.getTitle());
        dto.setPeriodStart(r.getPeriodStart());
        dto.setPeriodEnd(r.getPeriodEnd());
        dto.setTotalAnalyses(r.getTotalAnalyses());
        dto.setAverageOverallScore(r.getAverageOverallScore());
        dto.setAverageHydrationScore(r.getAverageHydrationScore());
        dto.setAverageAcneScore(r.getAverageAcneScore());
        dto.setAveragePigmentationScore(r.getAveragePigmentationScore());
        dto.setGlobalSummary(r.getGlobalSummary());
        dto.setProgressNotes(r.getProgressNotes());
        dto.setPdfUrl(r.getPdfUrl());
        dto.setGeneratedAt(r.getGeneratedAt());
        return dto;
    }
}
