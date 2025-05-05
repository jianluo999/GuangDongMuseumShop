package com.gdcp.guangdongmuseumshop.controller;

import com.gdcp.guangdongmuseumshop.common.Result;
import com.gdcp.guangdongmuseumshop.dto.UserProfileDTO;
import com.gdcp.guangdongmuseumshop.dto.UserSearchDTO;
import com.gdcp.guangdongmuseumshop.dto.PasswordUpdateDTO;
import com.gdcp.guangdongmuseumshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ROLE_USER')")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/list")
    public Result<List<UserProfileDTO>> getUserList() {
        log.info("获取用户列表");
        try {
            List<UserProfileDTO> users = userService.getAllUsers();
            return Result.success(users);
        } catch (Exception e) {
            log.error("获取用户列表失败:", e);
            return Result.error(500, "获取用户列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/profile")
    public Result<?> getUserProfile(Authentication authentication) {
        if (authentication == null) {
            return Result.error(401, "用户未登录");
        }
        
        String username = authentication.getName();
        log.debug("获取用户个人信息, 用户名: {}", username);
        
        UserProfileDTO profile = userService.getUserProfile(username);
        return Result.success(profile);
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(Authentication authentication, @RequestBody UserProfileDTO profileDTO) {
        if (authentication == null) {
            return Result.error(401, "用户未登录");
        }

        try {
            String username = authentication.getName();
            log.debug("更新用户个人信息, 用户名: {}", username);
            
            UserProfileDTO profile = userService.getUserProfile(username);
            profileDTO.setId(profile.getId()); // 设置正确的用户ID
            
            UserProfileDTO updatedProfile = userService.updateProfile(profile.getId(), profileDTO);
            return Result.success(updatedProfile);
        } catch (Exception e) {
            log.error("更新个人信息失败:", e);
            return Result.error(500, "更新个人信息失败：" + e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ROLE_USER')")
    public Result searchUsers(UserSearchDTO searchDTO, Pageable pageable) {
        log.info("开始查询用户列表");
        log.debug("查询参数: searchDTO={}, pageable={}", searchDTO, pageable);
        
        try {
            Page<UserProfileDTO> result = userService.searchUsers(searchDTO, pageable);
            log.info("查询成功，总记录数: {}", result.getTotalElements());
            log.debug("查询结果: {}", result.getContent());
            return Result.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "size", result.getSize(),
                "number", result.getNumber()
            ));
        } catch (Exception e) {
            log.error("查询用户列表失败:", e);
            return Result.error(500, "查询失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/enable")
    public Result<?> enableUser(@PathVariable Long id) {
        userService.enableUser(id);
        return Result.success("用户已启用");
    }

    @PutMapping("/{id}/disable")
    public Result<?> disableUser(@PathVariable Long id) {
        userService.disableUser(id);
        return Result.success("用户已禁用");
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("用户已删除");
    }

    @PostMapping
    public Result<?> createUser(@RequestBody UserProfileDTO userDTO) {
        log.info("开始创建用户");
        log.debug("用户信息: {}", userDTO);
        
        try {
            UserProfileDTO createdUser = userService.createUser(userDTO);
            log.info("用户创建成功, id: {}", createdUser.getId());
            return Result.success(createdUser);
        } catch (Exception e) {
            log.error("创建用户失败:", e);
            return Result.error(500, "创建用户失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/password/reset")
    public Result<?> resetPassword(@PathVariable Long id) {
        log.info("开始重置用户密码, id: {}", id);
        try {
            Map<String, String> result = userService.resetPassword(id);
            return Result.success(result);
        } catch (Exception e) {
            log.error("重置密码失败:", e);
            return Result.error(500, "重置密码失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody UserProfileDTO userDTO) {
        log.info("开始更新用户信息, id: {}", id);
        try {
            userDTO.setId(id);
            UserProfileDTO updatedUser = userService.updateUser(id, userDTO);
            return Result.success(updatedUser);
        } catch (Exception e) {
            log.error("更新用户失败:", e);
            return Result.error(500, "更新用户失败：" + e.getMessage());
        }
    }

    @PutMapping("/password")
    public Result<?> updatePassword(Authentication authentication, @RequestBody PasswordUpdateDTO passwordDTO) {
        if (authentication == null) {
            return Result.error(401, "用户未登录");
        }

        try {
            String username = authentication.getName();
            log.debug("更新用户密码, 用户名: {}", username);
            
            UserProfileDTO profile = userService.getUserProfile(username);
            userService.updatePassword(profile.getId(), passwordDTO);
            
            return Result.success("密码修改成功");
        } catch (Exception e) {
            log.error("修改密码失败:", e);
            return Result.error(500, "修改密码失败：" + e.getMessage());
        }
    }
} 