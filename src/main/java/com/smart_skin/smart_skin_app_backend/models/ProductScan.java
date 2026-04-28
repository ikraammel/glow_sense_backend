package com.smart_skin.smart_skin_app_backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_scans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductScan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String productName;

    @Column(name = "safety_rating")
    private String safetyRating;

    @Column
    private String brand;

    @Column
    private String barcode;

    @Column
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String ingredients; // liste brute des ingrédients

    @Column
    private Boolean compatible; // compatible avec le type de peau de l'utilisateur

    @Column
    private Integer compatibilityScore; // 0-100

    @Column(columnDefinition = "TEXT")
    private String positiveIngredients; // ingrédients bénéfiques JSON list

    @Column(columnDefinition = "TEXT")
    private String negativeIngredients; // ingrédients néfastes JSON list

    @Column(columnDefinition = "TEXT")
    private String analysisResult; // résumé IA de l'analyse

    @Column(nullable = false, updatable = false)
    private LocalDateTime scannedAt;

    @PrePersist
    protected void onCreate() {
        scannedAt = LocalDateTime.now();
    }
}
