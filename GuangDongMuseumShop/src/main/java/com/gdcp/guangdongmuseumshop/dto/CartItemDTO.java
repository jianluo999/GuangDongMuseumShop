package com.gdcp.guangdongmuseumshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemDTO {
    private Long itemId;
    private Long productId;
    
    @Min(value = 1, message = "商品数量必须大于0")
    private Integer quantity;
    
    private Boolean selected;
    
    private String productName;
    private String productImage;
    private Double productPrice;
    private Double totalPrice;
    private Integer stock;
    private String specification;
    private Long storeId;
    private String storeName;
} 