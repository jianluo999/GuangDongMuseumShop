package com.gdcp.guangdongmuseumshop.controller;

import com.gdcp.guangdongmuseumshop.common.Result;
import com.gdcp.guangdongmuseumshop.dto.stats.DailySalesStatsDTO;
import com.gdcp.guangdongmuseumshop.dto.stats.MonthlySalesStatsDTO;
import com.gdcp.guangdongmuseumshop.dto.stats.ProductStatsDTO;
import com.gdcp.guangdongmuseumshop.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminStatsController {

    private final StatsService statsService;

    @GetMapping("/sales/daily")
    public Result<List<DailySalesStatsDTO>> getDailySalesStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return Result.success(statsService.getDailySalesStats(startTime, endTime));
    }

    @GetMapping("/sales/monthly")
    public Result<List<MonthlySalesStatsDTO>> getMonthlySalesStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return Result.success(statsService.getMonthlySalesStats(startTime, endTime));
    }

    @GetMapping("/products/top-selling")
    public Result<List<ProductStatsDTO>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(statsService.getTopSellingProducts(limit));
    }

    @GetMapping("/products/category/{categoryId}")
    public Result<List<ProductStatsDTO>> getProductStatsByCategory(
            @PathVariable Long categoryId) {
        return Result.success(statsService.getProductStatsByCategory(categoryId));
    }
} 