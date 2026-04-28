package com.smart_skin.smart_skin_app_backend.dto;

import com.smart_skin.smart_skin_app_backend.enums.Severity;
import lombok.Data;

@Data
public class SkinProblemDto {
    private Long id;
    private String problemType;
    private Severity severity;
    private String zone;
    private String description;
    private Double confidence;
}
