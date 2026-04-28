package com.smart_skin.smart_skin_app_backend.repos;

import com.smart_skin.smart_skin_app_backend.models.CoachMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoachMessageRepository extends JpaRepository<CoachMessage, Long> {
    List<CoachMessage> findByUserIdAndSessionIdOrderBySentAtAsc(Long userId, String sessionId);
    List<CoachMessage> findByUserIdOrderBySentAtDesc(Long userId);
    void deleteByUserIdAndSessionId(Long userId, String sessionId);
}
