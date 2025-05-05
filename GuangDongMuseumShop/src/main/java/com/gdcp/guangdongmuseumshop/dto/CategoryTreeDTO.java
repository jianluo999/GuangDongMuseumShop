package com.gdcp.guangdongmuseumshop.dto;

import lombok.Data;
import java.util.List;

@Data
public class CategoryTreeDTO {
    private Long id;
    private String name;
    private String description;
    private Integer sort;
    private Boolean enabled;
    private List<CategoryTreeDTO> children;
} 