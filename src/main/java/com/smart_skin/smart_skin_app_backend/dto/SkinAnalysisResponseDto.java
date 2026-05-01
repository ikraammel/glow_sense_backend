package com.smart_skin.smart_skin_app_backend.dto;

import com.smart_skin.smart_skin_app_backend.enums.SkinType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkinAnalysisResponseDto {
    private Long id;
    private String imageUrl;
    private SkinType detectedSkinType;
    private Integer overallScore;
    private Integer hydrationScore;
    private Integer acneScore;
    private Integer pigmentationScore;
    private Integer wrinkleScore;
    private Integer poreScore;
    private String analysisDescription;
    private List<SkinProblemDto> detectedProblems;
    private List<RecommandationDto> recommandations;
    private LocalDateTime analyzedAt;

    private String modelPrediction;        // "acne", "dark spots", etc.
    private Double modelConfidence;        // 87.5
    private Map<String, Double> modelProbabilities;  // toutes les proba
    private Boolean modelWasAvailable;
    // alias used in service mapper
    public void setWrainkleScore(Integer v) { this.wrinkleScore = v; }
}
