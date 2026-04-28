package com.smart_skin.smart_skin_app_backend.services;

import com.smart_skin.smart_skin_app_backend.dto.ProductScanRequestDto;
import com.smart_skin.smart_skin_app_backend.dto.ProductScanResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ProductScanService {
    ProductScanResponseDto scanProduct(Long userId, ProductScanRequestDto request);
    ProductScanResponseDto scanProductByImage(Long userId, MultipartFile image);
    Page<ProductScanResponseDto> getScanHistory(Long userId, Pageable pageable);
}
