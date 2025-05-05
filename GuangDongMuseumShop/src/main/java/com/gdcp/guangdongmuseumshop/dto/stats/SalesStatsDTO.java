package com.gdcp.guangdongmuseumshop.dto.stats;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SalesStatsDTO {
    private LocalDateTime date;
    private Long orderCount;
    private BigDecimal totalAmount;
    private Long productCount;
    private Long userCount;

    public SalesStatsDTO() {}

    // 用于 JPQL 查询的构造函数
    public SalesStatsDTO(LocalDateTime date, Long orderCount, BigDecimal totalAmount) {
        this.date = date;
        this.orderCount = orderCount;
        this.totalAmount = totalAmount;
        this.productCount = 0L;
        this.userCount = 0L;
    }
} 