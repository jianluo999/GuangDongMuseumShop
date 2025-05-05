package com.gdcp.guangdongmuseumshop.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentStatusVO {
    private String status; // PENDING, SUCCESS, FAILED, CANCELLED
    private String message;
    private Long orderId;
    private BigDecimal amount;
    private String paymentMethod;
    private Long paymentTime;
} 