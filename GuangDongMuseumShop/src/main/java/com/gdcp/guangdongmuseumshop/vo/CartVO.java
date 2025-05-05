package com.gdcp.guangdongmuseumshop.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CartVO {
    private Long id;
    private String userId;
    private String status;
    private Integer itemCount;
    private Double totalAmount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 