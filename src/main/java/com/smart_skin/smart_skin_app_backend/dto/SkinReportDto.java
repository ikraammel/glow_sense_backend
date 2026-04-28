package com.smart_skin.smart_skin_app_backend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SkinReportDto {
    private Long id;
    private String title;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Integer totalAnalyses;
    private Double averageOverallScore;
    private Double averageHydrationScore;
    private Double averageAcneScore;
    private Double averagePigmentationScore;
    private String globalSummary;
    private String progressNotes;
    private String pdfUrl;
    private LocalDateTime generatedAt;
}
