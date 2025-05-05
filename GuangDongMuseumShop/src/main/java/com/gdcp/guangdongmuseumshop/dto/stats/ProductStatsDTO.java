package com.gdcp.guangdongmuseumshop.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatsDTO {
    private Long id;
    private String name;
    private Long quantity;
    private BigDecimal totalAmount;
} 