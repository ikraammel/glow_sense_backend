package com.smart_skin.smart_skin_app_backend.services;

import com.smart_skin.smart_skin_app_backend.dto.SkinAnalysisResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SkinAnalysisService {
    SkinAnalysisResponseDto analyzeImage(Long userId, MultipartFile image);
    SkinAnalysisResponseDto getAnalysisById(Long userId, Long analysisId);
    Page<SkinAnalysisResponseDto> getAnalysisHistory(Long userId, Pageable pageable);
    List<SkinAnalysisResponseDto> getRecentAnalyses(Long userId, int limit);
    void deleteAnalysis(Long userId, Long analysisId);
}
