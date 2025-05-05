package com.gdcp.guangdongmuseumshop.utils;

import com.gdcp.guangdongmuseumshop.entity.User;
import com.gdcp.guangdongmuseumshop.security.UserDetailsImpl;
import com.gdcp.guangdongmuseumshop.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {
        // 私有构造函数防止实例化
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     * @throws UnauthorizedException 如果用户未登录
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("用户未登录");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) principal).getId();
        }
        
        throw new UnauthorizedException("无法获取用户信息");
    }

    /**
     * 检查当前用户是否已登录
     *
     * @return true 如果用户已登录
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
} 