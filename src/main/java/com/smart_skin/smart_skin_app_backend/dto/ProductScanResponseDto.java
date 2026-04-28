package com.smart_skin.smart_skin_app_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductScanResponseDto {
    private Long id;
    private String productName;
    private String brand;
    private Boolean compatible;
    private Integer compatibilityScore;
    private String safetyRating;
    private List<String> positiveIngredients;
    private List<String> negativeIngredients;
    private String analysisResult;
    private LocalDateTime scannedAt;
}
