package com.smart_skin.smart_skin_app_backend.models;

import com.smart_skin.smart_skin_app_backend.enums.Severity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "skin_problems")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SkinProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skin_analysis_id", nullable = false)
    private SkinAnalysis skinAnalysis;

    @Column(nullable = false)
    private String problemType; // acne, tache, ride, pore, deshydratation, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column
    private String zone; // front, joues, nez, menton, etc.

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private Double confidence; // confidence score du modèle IA (0.0-1.0)
}
