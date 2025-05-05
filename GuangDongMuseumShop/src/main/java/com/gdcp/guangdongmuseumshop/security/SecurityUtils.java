package com.gdcp.guangdongmuseumshop.security;

import com.gdcp.guangdongmuseumshop.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdcp.guangdongmuseumshop.security.UserDetailsImpl;

public class SecurityUtils {
    
    private static final Logger log = LoggerFactory.getLogger(SecurityUtils.class);
    
    public static Long getCurrentUserId() {
        log.info("开始获取当前用户ID");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("当前认证对象: {}", authentication);
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("用户未认证");
            throw new UnauthorizedException("用户未认证");
        }

        Object principal = authentication.getPrincipal();
        log.info("认证主体: {}", principal);
        
        if (principal instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) principal).getId();
        }
        
        log.error("无法获取用户ID，认证主体类型不正确");
        throw new UnauthorizedException("无法获取用户ID");
    }

    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("用户未登录");
        }
        return auth.getName();
    }
} 