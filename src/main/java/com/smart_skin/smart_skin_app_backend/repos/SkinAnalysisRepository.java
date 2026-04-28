package com.smart_skin.smart_skin_app_backend.repos;

import com.smart_skin.smart_skin_app_backend.models.SkinAnalysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SkinAnalysisRepository extends JpaRepository<SkinAnalysis, Long> {

    Page<SkinAnalysis> findByUserIdOrderByAnalyzedAtDesc(Long userId, Pageable pageable);

    List<SkinAnalysis> findByUserIdOrderByAnalyzedAtDesc(Long userId);

    Optional<SkinAnalysis> findFirstByUserIdOrderByAnalyzedAtDesc(Long userId);

    List<SkinAnalysis> findByUserIdAndAnalyzedAtBetweenOrderByAnalyzedAtAsc(
            Long userId, LocalDateTime start, LocalDateTime end);

    long countByUserId(Long userId);

    @Query("SELECT sa FROM SkinAnalysis sa WHERE sa.user.id = :userId ORDER BY sa.analyzedAt DESC LIMIT :limit")
    List<SkinAnalysis> findRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Query("SELECT AVG(sa.overallScore) FROM SkinAnalysis sa WHERE sa.user.id = :userId")
    Double findAverageScoreByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT a FROM SkinAnalysis a
        LEFT JOIN FETCH a.detectedProblems
        WHERE a.user.id = :userId
        ORDER BY a.analyzedAt DESC
    """)
    List<SkinAnalysis> findFullByUserId(@Param("userId") Long userId);
}
