package com.smart_skin.smart_skin_app_backend.repos;

import com.smart_skin.smart_skin_app_backend.models.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    long countByUserIdAndReadAtIsNull(Long userId);

    @Modifying
    @Query("""
    UPDATE Notification n
    SET n.readAt = CURRENT_TIMESTAMP
    WHERE n.user.id = :userId
    AND n.readAt IS NULL
    """)
    void markAllAsReadByUserId(@Param("userId") Long userId);
}
