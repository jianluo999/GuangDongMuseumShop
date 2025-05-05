package com.gdcp.guangdongmuseumshop.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class UserRoleDTO {
    @NotEmpty(message = "角色列表不能为空")
    private Set<String> roles;
} 