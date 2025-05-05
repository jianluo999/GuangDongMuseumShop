package com.gdcp.guangdongmuseumshop.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDetailDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Boolean enabled;
    private String categoryName;
    private Long categoryId;
    private List<String> imageUrls;
    private Double averageRating;
    private Long reviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 