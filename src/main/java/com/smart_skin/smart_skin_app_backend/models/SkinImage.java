package com.smart_skin.smart_skin_app_backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "skin_images")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SkinImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String cloudinaryUrl;

    @Column(nullable = false)
    private String cloudinaryPublicId;

    @Column
    private String thumbnailUrl;

    @Column
    private Long fileSize;

    @Column
    private String format; // jpg, png, webp

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}
