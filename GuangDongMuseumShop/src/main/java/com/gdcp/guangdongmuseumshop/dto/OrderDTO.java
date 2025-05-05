package com.gdcp.guangdongmuseumshop.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private Long userId;
    private String username;
    private String orderNumber;
    private BigDecimal totalAmount;
    private String status;
    private String paymentMethod;
    private LocalDateTime paymentTime;
    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;
    private String shippingCompany;
    private String trackingNumber;
    private LocalDateTime shippingTime;
    private LocalDateTime completionTime;
    private List<OrderItemDTO> orderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 