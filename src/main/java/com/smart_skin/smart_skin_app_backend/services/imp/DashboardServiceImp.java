package com.smart_skin.smart_skin_app_backend.services.imp;

import com.smart_skin.smart_skin_app_backend.dto.*;
import com.smart_skin.smart_skin_app_backend.enums.Severity;
import com.smart_skin.smart_skin_app_backend.mappers.SkinAnalysisMapper;
import com.smart_skin.smart_skin_app_backend.models.SkinAnalysis;
import com.smart_skin.smart_skin_app_backend.models.SkinProblem;
import com.smart_skin.smart_skin_app_backend.repos.NotificationRepository;
import com.smart_skin.smart_skin_app_backend.repos.SkinAnalysisRepository;
import com.smart_skin.smart_skin_app_backend.services.DashboardService;
import com.smart_skin.smart_skin_app_backend.services.SkinAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImp implements DashboardService {

    private final SkinAnalysisRepository skinAnalysisRepository;
    private final SkinAnalysisService skinAnalysisService;
    private final NotificationRepository notificationRepository;
    private final SkinAnalysisMapper skinAnalysisMapper;

    @Override
    public DashboardDto getDashboard(Long userId) {
        List<SkinAnalysis> allAnalyses = skinAnalysisRepository.findFullByUserId(userId);

        long total = allAnalyses.size();
        Double avgScore = total > 0
                ? allAnalyses.stream().mapToInt(a -> a.getOverallScore() != null ? a.getOverallScore() : 0).average().orElse(0)
                : 0.0;

        // Score evolution: compare last 7 days vs previous 7 days
        double evolution = computeEvolution(allAnalyses);

        // Current streak (consecutive days with at least one analysis)
        int streak = computeStreak(allAnalyses);

        SkinAnalysis last = allAnalyses.isEmpty() ? null : allAnalyses.get(0);
        SkinAnalysisResponseDto lastAnalysis = skinAnalysisMapper.toDto(last);

        // Score history for chart (last 30 days)
        List<ScorePointDto> scoreHistory = allAnalyses.stream()
                .filter(a -> a.getAnalyzedAt().isAfter(LocalDateTime.now().minusDays(30)))
                .sorted(Comparator.comparing(SkinAnalysis::getAnalyzedAt))
                .map(a -> new ScorePointDto(
                        a.getAnalyzedAt(),
                        a.getOverallScore(),
                        a.getHydrationScore(),
                        a.getAcneScore(),
                        a.getPigmentationScore()))
                .collect(Collectors.toList());

        // Top problems
        List<ProblemFrequencyDto> topProblems = computeTopProblems(allAnalyses);

        // Latest recommendations from most recent analysis
        List<RecommandationDto> latestRecs = lastAnalysis != null
                ? lastAnalysis.getRecommandations()
                : List.of();

        long unread;
        try {
            unread = notificationRepository.countByUserIdAndReadAtIsNull(userId);
        } catch (Exception e) {
            unread = 0;
        }

        return DashboardDto.builder()
                .totalAnalyses((int) total)
                .averageScore(avgScore)
                .scoreEvolution(evolution)
                .currentStreak(streak)
                .lastAnalysis(lastAnalysis)
                .scoreHistory(scoreHistory)
                .topProblems(topProblems)
                .latestRecommandations(latestRecs != null ? latestRecs.stream().limit(3).collect(Collectors.toList()) : List.of())
                .unreadNotifications((int) unread)
                .build();
    }

    private double computeEvolution(List<SkinAnalysis> analyses) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        LocalDateTime fourteenDaysAgo = now.minusDays(14);

        OptionalDouble recent = analyses.stream()
                .filter(a -> a.getAnalyzedAt().isAfter(sevenDaysAgo))
                .mapToInt(a -> a.getOverallScore() != null ? a.getOverallScore() : 0)
                .average();

        OptionalDouble previous = analyses.stream()
                .filter(a -> a.getAnalyzedAt().isAfter(fourteenDaysAgo) && a.getAnalyzedAt().isBefore(sevenDaysAgo))
                .mapToInt(a -> a.getOverallScore() != null ? a.getOverallScore() : 0)
                .average();

        if (recent.isPresent() && previous.isPresent() && previous.getAsDouble() > 0) {
            return Math.round(((recent.getAsDouble() - previous.getAsDouble()) / previous.getAsDouble()) * 1000.0) / 10.0;
        }
        return 0.0;
    }

    private int computeStreak(List<SkinAnalysis> analyses) {
        if (analyses.isEmpty()) return 0;

        Set<String> analysisDays = analyses.stream()
                .map(a -> a.getAnalyzedAt().toLocalDate().toString())
                .collect(Collectors.toSet());

        int streak = 0;
        java.time.LocalDate day = java.time.LocalDate.now();
        while (analysisDays.contains(day.toString())) {
            streak++;
            day = day.minusDays(1);
        }
        return streak;
    }

    private List<ProblemFrequencyDto> computeTopProblems(List<SkinAnalysis> analyses) {
        Map<String, List<SkinProblem>> grouped = analyses.stream()
                .filter(a -> a.getDetectedProblems() != null)
                .flatMap(a -> a.getDetectedProblems().stream())
                .collect(Collectors.groupingBy(SkinProblem::getProblemType));

        return grouped.entrySet().stream()
                .map(e -> {
                    double avgSeverity = e.getValue().stream()
                            .mapToInt(p -> severityToInt(p.getSeverity()))
                            .average().orElse(1.0);
                    return new ProblemFrequencyDto(e.getKey(), (long) e.getValue().size(), avgSeverity);
                })
                .sorted(Comparator.comparingLong(ProblemFrequencyDto::getCount).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private int severityToInt(Severity s) {
        if (s == null) return 1;
        return switch (s) {
            case LEGERE -> 1;
            case MODEREE -> 2;
            case SEVERE -> 3;
        };
    }
}
