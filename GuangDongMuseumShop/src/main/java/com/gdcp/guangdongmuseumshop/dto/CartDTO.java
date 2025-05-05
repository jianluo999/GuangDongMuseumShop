package com.gdcp.guangdongmuseumshop.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartDTO {
    private Long id;
    private Long userId;
    private String username;
    private Integer itemCount;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<CartItemDTO> items;
} 