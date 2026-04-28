package com.smart_skin.smart_skin_app_backend.controllers;

import com.smart_skin.smart_skin_app_backend.dto.ApiResponse;
import com.smart_skin.smart_skin_app_backend.dto.SkinAnalysisResponseDto;
import com.smart_skin.smart_skin_app_backend.models.User;
import com.smart_skin.smart_skin_app_backend.services.SkinAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/analyses")
@RequiredArgsConstructor
public class SkinAnalysisController {

    private final SkinAnalysisService skinAnalysisService;

    /**
     * POST /api/analyses
     * Upload skin image → AI analysis → Returns full result
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SkinAnalysisResponseDto>> analyze(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("image") MultipartFile image) {
        SkinAnalysisResponseDto result = skinAnalysisService.analyzeImage(currentUser.getId(), image);
        return ResponseEntity.ok(ApiResponse.ok(result, "Analyse cutanée terminée"));
    }

    /**
     * GET /api/analyses?page=0&size=10
     * Paginated history
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SkinAnalysisResponseDto>>> getHistory(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(
                skinAnalysisService.getAnalysisHistory(currentUser.getId(), pageable)));
    }

    /**
     * GET /api/analyses/recent?limit=5
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<SkinAnalysisResponseDto>>> getRecent(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(
                skinAnalysisService.getRecentAnalyses(currentUser.getId(), limit)));
    }

    /**
     * GET /api/analyses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SkinAnalysisResponseDto>> getById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
                skinAnalysisService.getAnalysisById(currentUser.getId(), id)));
    }

    /**
     * DELETE /api/analyses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        skinAnalysisService.deleteAnalysis(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Analyse supprimée"));
    }
}
