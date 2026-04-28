package com.smart_skin.smart_skin_app_backend.repos;

import com.smart_skin.smart_skin_app_backend.models.SkinReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkinReportRepository extends JpaRepository<SkinReport, Long> {
    Page<SkinReport> findByUserIdOrderByGeneratedAtDesc(Long userId, Pageable pageable);
}
