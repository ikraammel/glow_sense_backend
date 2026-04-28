package com.smart_skin.smart_skin_app_backend.repos;

import com.smart_skin.smart_skin_app_backend.models.ProductScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductScanRepository extends JpaRepository<ProductScan, Long> {
    Page<ProductScan> findByUserIdOrderByScannedAtDesc(Long userId, Pageable pageable);
    List<ProductScan> findByUserIdOrderByScannedAtDesc(Long userId);
}
