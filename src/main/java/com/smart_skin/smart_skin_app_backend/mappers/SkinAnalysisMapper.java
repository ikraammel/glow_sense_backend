package com.smart_skin.smart_skin_app_backend.mappers;

import com.smart_skin.smart_skin_app_backend.dto.SkinAnalysisResponseDto;
import com.smart_skin.smart_skin_app_backend.models.SkinAnalysis;
import org.springframework.stereotype.Component;

@Component
public class SkinAnalysisMapper {

    public SkinAnalysisResponseDto toDto(SkinAnalysis a) {
        if (a == null) return null;

        return SkinAnalysisResponseDto.builder()
                .id(a.getId())
                .overallScore(a.getOverallScore())
                .hydrationScore(a.getHydrationScore())
                .acneScore(a.getAcneScore())
                .pigmentationScore(a.getPigmentationScore())
                .analysisDescription(a.getAnalysisDescription())
                .analyzedAt(a.getAnalyzedAt())
                .build();
    }
}