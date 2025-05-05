package com.gdcp.guangdongmuseumshop.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySalesStatsDTO {
    private Integer year;
    private Integer month;
    private Long orderCount;
    private BigDecimal totalAmount;
} 