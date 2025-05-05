package com.gdcp.guangdongmuseumshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefundRequestDTO {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
    
    @NotBlank(message = "退款原因不能为空")
    private String reason;
} 