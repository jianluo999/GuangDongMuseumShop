package com.gdcp.guangdongmuseumshop.enums;

public enum PaymentStatus {
    PENDING("待支付"),
    PAID("已支付"),
    REFUND_PENDING("退款中"),
    REFUNDED("已退款"),
    FAILED("支付失败"),
    CANCELLED("已取消");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 