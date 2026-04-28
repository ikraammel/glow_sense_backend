package com.smart_skin.smart_skin_app_backend.services;

import com.smart_skin.smart_skin_app_backend.dto.DashboardDto;
import com.smart_skin.smart_skin_app_backend.dto.SkinReportDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DashboardService {
    DashboardDto getDashboard(Long userId);
}
