package com.smart_skin.smart_skin_app_backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data @Builder
public class DashboardDto {
    private Integer totalAnalyses;
    private Double averageScore;
    private Double scoreEvolution;        // % evolution vs last period
    private Integer currentStreak;        // consecutive days with analysis
    private SkinAnalysisResponseDto lastAnalysis;
    private List<ScorePointDto> scoreHistory;  // for chart
    private List<ProblemFrequencyDto> topProblems;
    private List<RecommandationDto> latestRecommandations;
    private Integer unreadNotifications;
}
