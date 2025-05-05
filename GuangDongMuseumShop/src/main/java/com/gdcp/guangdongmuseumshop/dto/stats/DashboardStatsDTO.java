package com.gdcp.guangdongmuseumshop.dto.stats;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsDTO {
    private long todayOrders;
    private double todayAmount;
    private double orderGrowth;
    private double todaySales;
    private double salesGrowth;
    private int totalProducts;
    private int activeProducts;
    private int totalUsers;
    private int newUsers;
} 