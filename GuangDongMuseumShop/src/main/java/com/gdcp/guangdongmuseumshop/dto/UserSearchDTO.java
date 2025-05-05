package com.gdcp.guangdongmuseumshop.dto;

import lombok.Data;

@Data
public class UserSearchDTO {
    private String username;
    private String email;
    private String phone;
    private Boolean enabled;
} 