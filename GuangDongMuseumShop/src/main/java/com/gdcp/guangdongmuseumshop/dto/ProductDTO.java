package com.gdcp.guangdongmuseumshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDTO {
    private Long id;

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "商品价格不能为空")
    @Positive(message = "商品价格必须大于0")
    private BigDecimal price;

    @NotNull(message = "商品库存不能为空")
    @Positive(message = "商品库存必须大于0")
    private Integer stock;

    private String mainImage;

    private String culturalBackground;

    @NotNull(message = "商品分类不能为空")
    private Long categoryId;

    private List<String> images;
    
    private String status;
} 