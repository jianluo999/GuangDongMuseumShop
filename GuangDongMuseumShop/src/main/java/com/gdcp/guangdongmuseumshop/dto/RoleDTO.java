package com.gdcp.guangdongmuseumshop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleDTO {
    private Long id;
    
    @NotBlank(message = "角色名称不能为空")
    private String name;
    
    private String description;
} 