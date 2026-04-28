package com.smart_skin.smart_skin_app_backend.controllers;

import com.smart_skin.smart_skin_app_backend.dto.ApiResponse;
import com.smart_skin.smart_skin_app_backend.dto.ProductScanRequestDto;
import com.smart_skin.smart_skin_app_backend.dto.ProductScanResponseDto;
import com.smart_skin.smart_skin_app_backend.models.User;
import com.smart_skin.smart_skin_app_backend.services.ProductScanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductScanController {

    private final ProductScanService productScanService;

    /**
     * POST /api/products/scan
     * Analyze product by ingredients text
     */
    @PostMapping("/scan")
    public ResponseEntity<ApiResponse<ProductScanResponseDto>> scanByIngredients(
            @AuthenticationPrincipal User currentUser,
            @RequestBody ProductScanRequestDto request) {
        return ResponseEntity.ok(ApiResponse.ok(
                productScanService.scanProduct(currentUser.getId(), request),
                "Analyse du produit terminée"));
    }

    /**
     * POST /api/products/scan/image
     * Analyze product by scanning label image
     */
    @PostMapping(value = "/scan/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductScanResponseDto>> scanByImage(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("image") MultipartFile image) {
        return ResponseEntity.ok(ApiResponse.ok(
                productScanService.scanProductByImage(currentUser.getId(), image),
                "Analyse du produit terminée"));
    }

    /**
     * GET /api/products/history?page=0&size=10
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<ProductScanResponseDto>>> getHistory(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                productScanService.getScanHistory(currentUser.getId(), PageRequest.of(page, size))));
    }
}
