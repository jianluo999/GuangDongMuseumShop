package com.gdcp.guangdongmuseumshop.controller;

import com.gdcp.guangdongmuseumshop.dto.LoginRequest;
import com.gdcp.guangdongmuseumshop.dto.RegisterRequest;
import com.gdcp.guangdongmuseumshop.dto.AuthResponse;
import com.gdcp.guangdongmuseumshop.service.AuthService;
import com.gdcp.guangdongmuseumshop.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/legacy/auth")
@RequiredArgsConstructor
public class LegacyAuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<?> register(@RequestBody @Valid RegisterRequest request) {
        try {
            log.info("接收到传统路径注册请求: {}", request.getUsername());
            AuthResponse response = authService.register(request);
            log.info("传统路径注册成功: {}", request.getUsername());
            return Result.success(new HashMap<String, Object>() {{
                put("token", response.getToken());
                put("username", response.getUsername());
                put("userId", response.getUserId());
            }});
        } catch (Exception e) {
            log.error("传统路径注册失败: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("接收到传统路径登录请求: {}", request.getUsername());
            AuthResponse response = authService.login(request);
            log.info("传统路径登录成功: {}", request.getUsername());
            return Result.success(new HashMap<String, Object>() {{
                put("token", response.getToken());
                put("username", response.getUsername());
                put("userId", response.getUserId());
            }});
        } catch (Exception e) {
            log.error("传统路径登录失败: {}", e.getMessage());
            return Result.error(401, e.getMessage());
        }
    }
} 