package com.gdcp.guangdongmuseumshop.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentQRCodeVO {
    private String qrCodeUrl;
    private String paymentMethod;
    private Long orderId;
    private BigDecimal amount;
    private Long expireTime;
} 