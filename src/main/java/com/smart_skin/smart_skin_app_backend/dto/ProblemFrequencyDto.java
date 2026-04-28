package com.smart_skin.smart_skin_app_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class ProblemFrequencyDto {
    private String problemType;
    private Long count;
    private Double averageSeverity;
}
