package com.gdcp.guangdongmuseumshop.dto;

import lombok.Data;

@Data
public class ProductSearchDTO {
    private String name;
    private Long categoryId;
    private String status;
} 