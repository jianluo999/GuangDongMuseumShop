package com.gdcp.guangdongmuseumshop.common;

public class Constants {
    // 系统相关
    public static final String API_PREFIX = "/api";
    
    // 用户相关
    public static final String DEFAULT_AVATAR = "https://example.com/default-avatar.png";
    
    // 商品相关
    public static final String DEFAULT_PRODUCT_IMAGE = "https://example.com/default-product.png";
    
    // 订单状态
    public static final int ORDER_STATUS_UNPAID = 0;
    public static final int ORDER_STATUS_PAID = 1;
    public static final int ORDER_STATUS_SHIPPED = 2;
    public static final int ORDER_STATUS_COMPLETED = 3;
    public static final int ORDER_STATUS_CANCELLED = 4;
} 