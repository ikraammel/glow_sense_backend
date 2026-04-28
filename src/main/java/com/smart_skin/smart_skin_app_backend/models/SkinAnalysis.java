package com.smart_skin.smart_skin_app_backend.models;

import com.smart_skin.smart_skin_app_backend.enums.SkinType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "skin_analyses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SkinAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "skin_image_id")
    private SkinImage skinImage;

    @Enumerated(EnumType.STRING)
    private SkinType detectedSkinType;

    // Scores globaux (0-100)
    @Column
    private Integer overallScore;

    @Column
    private Integer hydrationScore;

    @Column
    private Integer acneScore;

    @Column
    private Integer pigmentationScore;

    @Column
    private Integer wrinkleScore;

    @Column
    private Integer poreScore;

    // Description IA
    @Column(columnDefinition = "TEXT")
    private String analysisDescription;

    @Column(columnDefinition = "TEXT")
    private String aiRawResponse; // réponse brute du modèle IA

    @OneToMany(mappedBy = "skinAnalysis", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SkinProblem> detectedProblems;

    @OneToMany(mappedBy = "skinAnalysis", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Recommandation> recommandations;

    @Column(nullable = false, updatable = false)
    private LocalDateTime analyzedAt;

    @PrePersist
    protected void onCreate() {
        analyzedAt = LocalDateTime.now();
    }
}
