package com.smart_skin.smart_skin_app_backend.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recommandations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Recommandation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skin_analysis_id", nullable = false)
    private SkinAnalysis skinAnalysis;

    @Column(nullable = false)
    private String category; // nettoyant, hydratant, traitement, protection_solaire, alimentation, style_de_vie

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String productName;

    @Column
    private String activeIngredient; // niacinamide, salicylic_acid, retinol, etc.

    @Column
    private Integer priority; // 1 = haute, 2 = moyenne, 3 = basse

    @Column
    private String applicationFrequency; // matin, soir, matin_et_soir

    @Column
    private String tips; // conseil d'application
}
