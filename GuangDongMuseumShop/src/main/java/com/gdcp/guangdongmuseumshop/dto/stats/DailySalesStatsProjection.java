package com.gdcp.guangdongmuseumshop.dto.stats;

import java.time.LocalDate;
import java.math.BigDecimal;

public interface DailySalesStatsProjection {
    LocalDate getDate();
    Long getOrderCount();
    BigDecimal getTotalAmount();
} 