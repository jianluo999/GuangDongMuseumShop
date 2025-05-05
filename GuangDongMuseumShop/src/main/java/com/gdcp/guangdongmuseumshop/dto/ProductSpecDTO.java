package com.gdcp.guangdongmuseumshop.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductSpecDTO {
    private Long id;
    private String name;
    private List<SpecOption> options;
    private Integer sortOrder;

    @Data
    public static class SpecOption {
        private String value;
        private String label;
        private Integer stock;
        private Boolean enabled;
    }
} 