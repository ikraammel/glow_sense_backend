package com.smart_skin.smart_skin_app_backend.controllers;

import com.smart_skin.smart_skin_app_backend.dto.ApiResponse;
import com.smart_skin.smart_skin_app_backend.dto.SkinReportDto;
import com.smart_skin.smart_skin_app_backend.models.User;
import com.smart_skin.smart_skin_app_backend.services.SkinReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class SkinReportController {

    private final SkinReportService skinReportService;

    /**
     * POST /api/reports/generate?from=2024-01-01&to=2024-01-31
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<SkinReportDto>> generate(
            @AuthenticationPrincipal User currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.ok(
                skinReportService.generateReport(currentUser.getId(), from, to),
                "Rapport généré avec succès"));
    }

    /**
     * GET /api/reports?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SkinReportDto>>> getReports(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                skinReportService.getReports(currentUser.getId(), PageRequest.of(page, size))));
    }

    /**
     * GET /api/reports/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SkinReportDto>> getReport(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
                skinReportService.getReport(currentUser.getId(), id)));
    }
}
