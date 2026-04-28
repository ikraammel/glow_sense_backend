package com.smart_skin.smart_skin_app_backend.dto;

import lombok.Data;

@Data
public class RecommandationDto {
    private Long id;
    private String category;
    private String title;
    private String description;
    private String productName;
    private String activeIngredient;
    private Integer priority;
    private String applicationFrequency;
    private String tips;
}
