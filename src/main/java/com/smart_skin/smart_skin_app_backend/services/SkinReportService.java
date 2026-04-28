package com.smart_skin.smart_skin_app_backend.services;

import com.smart_skin.smart_skin_app_backend.dto.SkinReportDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface SkinReportService {
    SkinReportDto generateReport(Long userId, LocalDate from, LocalDate to);
    Page<SkinReportDto> getReports(Long userId, Pageable pageable);
    SkinReportDto getReport(Long userId, Long reportId);
}
