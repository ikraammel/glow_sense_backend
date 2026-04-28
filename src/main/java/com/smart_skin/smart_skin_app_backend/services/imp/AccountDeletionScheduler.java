package com.smart_skin.smart_skin_app_backend.services.imp;

import com.smart_skin.smart_skin_app_backend.repos.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountDeletionScheduler {

    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deleteExpiredAccounts() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        int deleted = userRepository.deleteAllByDeletionRequestedAtBefore(cutoff);
        if (deleted > 0) {
            log.info("Scheduled deletion: {} account(s) permanently deleted", deleted);
        }
    }
}
