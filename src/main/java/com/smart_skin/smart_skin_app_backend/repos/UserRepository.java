package com.smart_skin.smart_skin_app_backend.repos;

import com.smart_skin.smart_skin_app_backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Modifying
    @Query("DELETE FROM User u WHERE u.deletionRequestedAt IS NOT NULL AND u.deletionRequestedAt < :cutoff")
    int deleteAllByDeletionRequestedAtBefore(@Param("cutoff") LocalDateTime cutoff);
}
