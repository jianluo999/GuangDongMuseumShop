package com.gdcp.guangdongmuseumshop.dto.stats;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@NoArgsConstructor
public class DailySalesStatsDTO {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private Long orderCount;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal totalAmount;

    public DailySalesStatsDTO(LocalDate date, Long orderCount, BigDecimal totalAmount) {
        this.date = date;
        this.orderCount = orderCount;
        this.totalAmount = totalAmount;
    }

    public double getTotalAmount() {
        return totalAmount != null ? totalAmount.doubleValue() : 0.0;
    }
} 