package com.smart_skin.smart_skin_app_backend.models;

import com.smart_skin.smart_skin_app_backend.enums.SkinType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column
    private String phoneNumber;

    @Column
    private LocalDate dateOfBirth;

    @Column
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    private SkinType skinType;

    @Column
    private String skinConcerns; // comma-separated: acne,taches,rides

    @Column
    private String ethnicity;

    @Column
    private String routinePreference; // minimal, moderate, complete

    @Column
    private String effortLevel; // low, medium, high

    @Column
    private String sunExposure; // rare, moderate, frequent

    @Column
    private String ingredientsToAvoid;

    @Column
    private boolean notificationsEnabled = true;

    @Column(nullable = false)
    private boolean emailWeeklyDigest = true;

    @Column(nullable = false)
    private boolean emailAnalysisResults = true;

    @Column(nullable = false)
    private boolean emailSkincareTips = true;

    @Column(nullable = false)
    private boolean emailProductReviews = false;

    @Column(nullable = false)
    private boolean emailAccountUpdates = true;

    @Column(nullable = false)
    private boolean emailSecurityAlerts = true;

    @Column(nullable = false)
    private boolean emailPromotions = false;

    @Column
    private String digestDay = "Monday";

    @Column
    private String digestTime = "Morning";

    @Column(nullable = false)
    private boolean notifAnalysisReminders = true;

    @Column(nullable = false)
    private boolean notifWeeklyReports = true;

    @Column(nullable = false)
    private boolean notifNewRecommendations = true;

    @Column(nullable = false)
    private boolean notifRoutineReminders = false;

    @Column(nullable = false)
    private boolean notifProgressUpdates = true;

    @Column(nullable = false)
    private boolean notifProductAlerts = false;

    @Column(nullable = false)
    private boolean notifPromotionsPush = false;

    @Column
    private String reminderFrequency = "weekly";

    @Column
    private LocalDateTime deletionRequestedAt;

    @Column
    private String fcmToken; // Firebase Cloud Messaging token

    @Column
    private boolean onboardingCompleted = false;

    @Column
    private boolean emailVerified = false;

    @Column
    private String passwordResetToken;

    @Column
    private LocalDateTime passwordResetTokenExpiry;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SkinAnalysis> skinAnalyses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SkinReport> skinReports;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
