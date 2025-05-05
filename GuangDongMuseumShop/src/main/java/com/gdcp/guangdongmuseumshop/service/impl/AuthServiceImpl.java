package com.gdcp.guangdongmuseumshop.service.impl;

import com.gdcp.guangdongmuseumshop.security.JwtUtils;
import com.gdcp.guangdongmuseumshop.service.AuthService;
import com.gdcp.guangdongmuseumshop.dto.LoginRequest;
import com.gdcp.guangdongmuseumshop.dto.RegisterRequest;
import com.gdcp.guangdongmuseumshop.dto.AuthResponse;
import com.gdcp.guangdongmuseumshop.dto.LoginResult;

import com.gdcp.guangdongmuseumshop.entity.User;
import com.gdcp.guangdongmuseumshop.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("开始处理登录请求: {}", request.getUsername());
        try {
            User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            log.info("找到用户: {}", user.getUsername());
            
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("密码错误");
            }
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );
            log.info("用户认证成功: {}", authentication.getName());

            String token = jwtUtils.generateToken(authentication);
            log.info("Token生成成功");
            
            // 确保正确加载用户角色
            Set<String> roles = user.getRoles().stream()
                    .map(role -> role.getName().toUpperCase())
                    .collect(Collectors.toSet());
                    
            return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .userId(user.getId())
                .roles(roles)  // 返回角色信息
                .build();
        } catch (AuthenticationException e) {
            log.error("认证失败: ", e);
            throw new RuntimeException("用户名或密码错误");
        } catch (Exception e) {
            log.error("登录过程发生错误: ", e);
            log.error("错误详情: ", e);
            throw new RuntimeException("登录失败: " + e.getMessage());
        }
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        
        userRepository.save(user);
        return login(new LoginRequest(request.getUsername(), request.getPassword()));
    }

    @Override
    public LoginResult adminLogin(String username, String password) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("用户名不存在"));
        
        boolean isAdmin = user.getRoles().stream()
            .anyMatch(role -> role.getName().equals("ADMIN"));
        
        if (!isAdmin) {
            throw new AccessDeniedException("非管理员账号");
        }
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("密码错误");
        }
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null, user.getAuthorities());
        String token = jwtUtils.generateToken(authentication);
        
        return LoginResult.builder()
            .token(token)
            .userId(user.getId())
            .username(user.getUsername())
            .role("ADMIN")
            .build();
    }
} 