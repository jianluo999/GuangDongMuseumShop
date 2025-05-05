package com.gdcp.guangdongmuseumshop.controller;

import com.gdcp.guangdongmuseumshop.dto.LoginRequest;
import com.gdcp.guangdongmuseumshop.dto.RegisterRequest;
import com.gdcp.guangdongmuseumshop.dto.AuthResponse;
import com.gdcp.guangdongmuseumshop.service.AuthService;
import com.gdcp.guangdongmuseumshop.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "code", 400,
                    "message", "注册失败",
                    "error", e.getMessage()
                ));
        }
    }

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return Result.success(new HashMap<String, Object>() {{
                put("token", response.getToken());
                put("username", response.getUsername());
                put("userId", response.getUserId());
            }});
        } catch (Exception e) {
            return Result.error(401, e.getMessage());
        }
    }
} 