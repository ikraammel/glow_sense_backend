package com.smart_skin.smart_skin_app_backend.services.imp;

import com.smart_skin.smart_skin_app_backend.dto.NotificationDto;
import com.smart_skin.smart_skin_app_backend.enums.NotificationType;
import com.smart_skin.smart_skin_app_backend.exception.ResourceNotFoundException;
import com.smart_skin.smart_skin_app_backend.models.Notification;
import com.smart_skin.smart_skin_app_backend.models.SkinAnalysis;
import com.smart_skin.smart_skin_app_backend.models.User;
import com.smart_skin.smart_skin_app_backend.repos.NotificationRepository;
import com.smart_skin.smart_skin_app_backend.repos.UserRepository;
import com.smart_skin.smart_skin_app_backend.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImp implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public Page<NotificationDto> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toDto);
    }

    @Override
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndReadAtIsNull(userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvée"));
        if (!n.getUser().getId().equals(userId)) return;
        n.setSeen(true);
        n.setReadAt(LocalDateTime.now());
        notificationRepository.save(n);
    }

    @Override
    @Transactional
    public void createAnalysisCompleteNotification(User user, SkinAnalysis analysis) {
        if (!user.isNotificationsEnabled()) return;
        Notification n = Notification.builder()
                .user(user)
                .type(NotificationType.NEW_RECOMMENDATION)
                .title("Analyse terminée ✨")
                .message(String.format(
                        "Votre analyse cutanée est prête ! Score global : %d/100. Découvrez vos recommandations personnalisées.",
                        analysis.getOverallScore()))
                .build();
        notificationRepository.save(n);
    }

    @Override
    @Transactional
    public void createReportReadyNotification(User user, String reportTitle) {
        if (!user.isNotificationsEnabled()) return;
        Notification n = Notification.builder()
                .user(user)
                .type(NotificationType.REPORT_READY)
                .title("Rapport disponible 📊")
                .message("Votre rapport \"" + reportTitle + "\" a été généré et est prêt à consulter.")
                .build();
        notificationRepository.save(n);
    }

    // Every Monday at 9:00 AM — weekly analysis reminder
    @Scheduled(cron = "0 0 9 * * MON")
    @Override
    @Transactional
    public void sendWeeklyReminders() {
        List<User> users = userRepository.findAll().stream()
                .filter(User::isNotificationsEnabled)
                .filter(User::isOnboardingCompleted)
                .toList();

        for (User user : users) {
            Notification n = Notification.builder()
                    .user(user)
                    .type(NotificationType.ANALYSE_REMINDER)
                    .title("C'est l'heure de votre analyse ! 🔍")
                    .message("Effectuez votre analyse hebdomadaire pour suivre l'évolution de votre peau.")
                    .build();
            notificationRepository.save(n);
        }
        log.info("Weekly reminders sent to {} users", users.size());
    }

    private NotificationDto toDto(Notification n) {
        NotificationDto dto = new NotificationDto();
        dto.setId(n.getId());
        dto.setType(n.getType());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setSeen(n.isSeen());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}
