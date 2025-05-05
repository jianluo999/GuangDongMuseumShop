package com.gdcp.guangdongmuseumshop.dto;

import com.gdcp.guangdongmuseumshop.enums.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private Long id;
    private String paymentNo;
    private Long orderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String refundReason;
    private LocalDateTime paidTime;
    private LocalDateTime refundTime;
    private LocalDateTime createdAt;
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;
} 