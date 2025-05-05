package com.gdcp.guangdongmuseumshop.service;

import com.gdcp.guangdongmuseumshop.dto.LoginRequest;
import com.gdcp.guangdongmuseumshop.dto.RegisterRequest;
import com.gdcp.guangdongmuseumshop.dto.AuthResponse;
import com.gdcp.guangdongmuseumshop.dto.LoginResult;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    LoginResult adminLogin(String username, String password);
}