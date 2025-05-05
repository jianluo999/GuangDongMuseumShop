package com.gdcp.guangdongmuseumshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartItemQuantityDTO {
    @NotNull(message = "购物车项ID不能为空")
    private Long itemId;
    
    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量必须大于0")
    private Integer quantity;
} 