package com.gdcp.guangdongmuseumshop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long id;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private String description;

    private Long parentId;

    private Integer level;

    private Integer sort;
} 