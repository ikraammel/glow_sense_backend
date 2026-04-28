package com.smart_skin.smart_skin_app_backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "coach_messages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CoachMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String role; // "user" or "assistant"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column
    private String sessionId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }
}
