package com.smart_skin.smart_skin_app_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
public class ScorePointDto {
    private LocalDateTime date;
    private Integer overallScore;
    private Integer hydrationScore;
    private Integer acneScore;
    private Integer pigmentationScore;
}
