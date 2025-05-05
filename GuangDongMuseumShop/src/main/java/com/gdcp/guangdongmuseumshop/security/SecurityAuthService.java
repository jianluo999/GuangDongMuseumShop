package com.gdcp.guangdongmuseumshop.security;

import com.gdcp.guangdongmuseumshop.dto.LoginRequest;
import org.springframework.security.core.Authentication;

public interface SecurityAuthService {
    Authentication authenticate(LoginRequest loginRequest);
} 