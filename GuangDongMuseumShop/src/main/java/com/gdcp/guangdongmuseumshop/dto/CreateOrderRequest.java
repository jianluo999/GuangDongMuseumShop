package com.gdcp.guangdongmuseumshop.dto;

import lombok.Data;
import java.util.List;
import java.math.BigDecimal;

@Data
public class CreateOrderRequest {
    private List<OrderItemRequest> items;
    private ShippingInfo shipping;
    private BigDecimal totalAmount;
} 