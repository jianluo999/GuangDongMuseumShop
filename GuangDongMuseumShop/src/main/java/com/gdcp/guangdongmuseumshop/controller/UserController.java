package com.gdcp.guangdongmuseumshop.controller;

import com.gdcp.guangdongmuseumshop.common.Result;
import com.gdcp.guangdongmuseumshop.dto.PasswordUpdateDTO;
import com.gdcp.guangdongmuseumshop.dto.RegisterDTO;
import com.gdcp.guangdongmuseumshop.dto.UserDTO;
import com.gdcp.guangdongmuseumshop.dto.UserProfileDTO;
import com.gdcp.guangdongmuseumshop.security.SecurityUtils;
import com.gdcp.guangdongmuseumshop.dto.OrderSearchDTO;
import com.gdcp.guangdongmuseumshop.service.OrderService;
import com.gdcp.guangdongmuseumshop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final OrderService orderService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public Result<UserProfileDTO> getProfile() {
        log.info("开始获取用户信息");
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            log.info("当前用户ID: {}", userId);
            UserProfileDTO profile = userService.getUserProfile(userId);
            log.info("获取到用户信息: {}", profile);
            return Result.success(profile);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error(500, "获取用户信息失败: " + e.getMessage());
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public Result<UserProfileDTO> updateProfile(@Valid @RequestBody UserProfileDTO profileDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.updateProfile(userId, profileDTO));
    }

    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public Result<?> updatePassword(@Valid @RequestBody PasswordUpdateDTO passwordDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.updatePassword(userId, passwordDTO);
        return Result.success("密码修改成功");
    }

    @PostMapping("/avatar")
    @PreAuthorize("isAuthenticated()")
    public Result<String> updateAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtils.getCurrentUserId();
        String avatarUrl = userService.updateAvatar(userId, file);
        return Result.success(avatarUrl);
    }

    @GetMapping("/orders")
    public Result<?> getUserOrders(Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        OrderSearchDTO searchDTO = new OrderSearchDTO();
        searchDTO.setUsername(auth.getName());
        return Result.success(orderService.getOrders(searchDTO, pageable));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN') or #registerDTO.role == 'USER'")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return ResponseEntity.ok(userService.register(registerDTO));
    }
} 