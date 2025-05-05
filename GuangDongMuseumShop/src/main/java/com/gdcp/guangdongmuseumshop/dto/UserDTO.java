package com.gdcp.guangdongmuseumshop.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String avatarUrl;
    private Boolean enabled;
    private Set<String> roles;
    private LocalDateTime createdAt;
}