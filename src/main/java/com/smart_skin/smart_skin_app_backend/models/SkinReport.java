package com.smart_skin.smart_skin_app_backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "skin_reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SkinReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column
    private LocalDate periodStart;

    @Column
    private LocalDate periodEnd;

    @Column
    private Integer totalAnalyses;

    @Column
    private Double averageOverallScore;

    @Column
    private Double averageHydrationScore;

    @Column
    private Double averageAcneScore;

    @Column
    private Double averagePigmentationScore;

    @Column(columnDefinition = "TEXT")
    private String globalSummary;

    @Column(columnDefinition = "TEXT")
    private String progressNotes;

    @Column
    private String pdfUrl; // URL du rapport PDF généré sur Cloudinary

    @Column(nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @ManyToMany
    @JoinTable(
        name = "report_analyses",
        joinColumns = @JoinColumn(name = "report_id"),
        inverseJoinColumns = @JoinColumn(name = "analysis_id")
    )
    private List<SkinAnalysis> includedAnalyses;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
    }
}
