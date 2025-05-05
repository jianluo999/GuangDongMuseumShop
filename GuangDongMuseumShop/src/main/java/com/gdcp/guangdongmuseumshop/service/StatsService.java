package com.gdcp.guangdongmuseumshop.service;

import com.gdcp.guangdongmuseumshop.dto.stats.DailySalesStatsDTO;
import com.gdcp.guangdongmuseumshop.dto.stats.MonthlySalesStatsDTO;
import com.gdcp.guangdongmuseumshop.dto.stats.ProductStatsDTO;
import com.gdcp.guangdongmuseumshop.dto.stats.DashboardStatsDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    DashboardStatsDTO getDashboardStats();
    List<DailySalesStatsDTO> getDailySalesStats(LocalDateTime startTime, LocalDateTime endTime);
    List<MonthlySalesStatsDTO> getMonthlySalesStats(LocalDateTime startTime, LocalDateTime endTime);
    List<ProductStatsDTO> getTopSellingProducts(int limit);
    List<ProductStatsDTO> getProductStatsByCategory(Long categoryId);
    List<DailySalesStatsDTO> getSalesTrend(int days);
} 