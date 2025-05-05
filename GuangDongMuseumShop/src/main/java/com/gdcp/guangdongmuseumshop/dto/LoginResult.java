package com.gdcp.guangdongmuseumshop.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResult {
    private String token;
    private Long userId;
    private String username;
    private String role;
} 