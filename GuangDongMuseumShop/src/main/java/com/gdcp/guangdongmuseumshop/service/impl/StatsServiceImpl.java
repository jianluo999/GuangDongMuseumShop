package com.gdcp.guangdongmuseumshop.service.impl;

import com.gdcp.guangdongmuseumshop.dto.stats.*;
import com.gdcp.guangdongmuseumshop.repository.OrderRepository;
import com.gdcp.guangdongmuseumshop.repository.ProductRepository;
import com.gdcp.guangdongmuseumshop.repository.UserRepository;
import com.gdcp.guangdongmuseumshop.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public DashboardStatsDTO getDashboardStats() {
        LocalDateTime today = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime yesterday = today.minusDays(1);

        // 获取今日订单统计
        List<DailySalesStatsProjection> todayStats = orderRepository.getDailySalesStats(
            today, today.with(LocalTime.MAX)
        );
        
        // 获取昨日订单统计
        List<DailySalesStatsProjection> yesterdayStats = orderRepository.getDailySalesStats(
            yesterday, yesterday.with(LocalTime.MAX)
        );

        // 计算增长率
        double todayAmount = todayStats.isEmpty() ? 0 : todayStats.get(0).getTotalAmount().doubleValue();
        double yesterdayAmount = yesterdayStats.isEmpty() ? 0 : yesterdayStats.get(0).getTotalAmount().doubleValue();
        double orderGrowth = calculateGrowth(
            todayStats.isEmpty() ? 0 : todayStats.get(0).getOrderCount(),
            yesterdayStats.isEmpty() ? 0 : yesterdayStats.get(0).getOrderCount()
        );
        double salesGrowth = calculateGrowth(todayAmount, yesterdayAmount);

        return DashboardStatsDTO.builder()
            .todayOrders(todayStats.isEmpty() ? 0 : todayStats.get(0).getOrderCount())
            .todayAmount(todayAmount)
            .orderGrowth(orderGrowth)
            .todaySales(todayAmount)
            .salesGrowth(salesGrowth)
            .totalProducts((int) productRepository.count())
            .activeProducts((int) productRepository.countByEnabledTrue())
            .totalUsers((int) userRepository.count())
            .newUsers((int) userRepository.countByCreatedAtBetween(today, today.with(LocalTime.MAX)))
            .build();
    }

    @Override
    // @Cacheable(value = "dailySalesStats", key = "#startTime.toString() + #endTime.toString()")
    public List<DailySalesStatsDTO> getDailySalesStats(LocalDateTime startTime, LocalDateTime endTime) {
        List<DailySalesStatsProjection> projections = orderRepository.getDailySalesStats(startTime, endTime);
        return projections.stream()
            .map(p -> new DailySalesStatsDTO(p.getDate(), p.getOrderCount(), p.getTotalAmount()))
            .collect(Collectors.toList());
    }

    @Override
    // @Cacheable(value = "monthlySalesStats", key = "#startTime.toString() + #endTime.toString()")
    public List<MonthlySalesStatsDTO> getMonthlySalesStats(LocalDateTime startTime, LocalDateTime endTime) {
        return orderRepository.getMonthlySalesStats(startTime, endTime);
    }

    @Override
    // @Cacheable(value = "topSellingProducts", key = "#limit")
    public List<ProductStatsDTO> getTopSellingProducts(int limit) {
        return orderRepository.getTopSellingProducts(PageRequest.of(0, limit));
    }

    @Override
    @Cacheable(value = "categoryProductStats", key = "#categoryId")
    public List<ProductStatsDTO> getProductStatsByCategory(Long categoryId) {
        return orderRepository.getProductStatsByCategory(categoryId);
    }

    @Override
    public List<DailySalesStatsDTO> getSalesTrend(int days) {
        LocalDateTime endDate = LocalDateTime.now().with(LocalTime.MAX);
        LocalDateTime startDate = endDate.minusDays(days - 1).with(LocalTime.MIN);
        
        // 获取销售数据
        List<DailySalesStatsProjection> salesData = orderRepository.getDailySalesStats(startDate, endDate);
        
        // 创建完整的日期序列,确保每一天都有数据
        Map<LocalDate, DailySalesStatsDTO> statsMap = salesData.stream()
            .collect(Collectors.toMap(
                DailySalesStatsProjection::getDate,
                p -> new DailySalesStatsDTO(p.getDate(), p.getOrderCount(), p.getTotalAmount())
            ));
        
        List<DailySalesStatsDTO> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i).toLocalDate();
            DailySalesStatsDTO stats = statsMap.getOrDefault(date, 
                new DailySalesStatsDTO(date, 0L, BigDecimal.ZERO));
            result.add(stats);
        }
        
        return result;
    }

    private double calculateGrowth(double current, double previous) {
        if (previous == 0) return current > 0 ? 100 : 0;
        return ((current - previous) / previous) * 100;
    }
} 