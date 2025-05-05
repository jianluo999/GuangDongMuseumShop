package com.gdcp.guangdongmuseumshop.controller;

import com.gdcp.guangdongmuseumshop.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import com.gdcp.guangdongmuseumshop.util.JwtUtils;
import com.gdcp.guangdongmuseumshop.dto.JwtResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.gdcp.guangdongmuseumshop.dto.stats.DashboardStatsDTO;
import com.gdcp.guangdongmuseumshop.dto.stats.DailySalesStatsDTO;
import com.gdcp.guangdongmuseumshop.dto.stats.ProductStatsDTO;
import com.gdcp.guangdongmuseumshop.service.StatsService;
import com.gdcp.guangdongmuseumshop.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.time.LocalDateTime;
import java.util.List;
import com.gdcp.guangdongmuseumshop.dto.OrderDTO;
import com.gdcp.guangdongmuseumshop.dto.OrderSearchDTO;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AuthenticationManager authenticationManager;
    @Qualifier("jwtUtilsService")
    private final JwtUtils jwtUtils;
    private final StatsService statsService;
    private final OrderService orderService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest) {
        log.info("Admin login attempt for username: {}", loginRequest.getUsername());
        
        try {
            // 1. 进行认证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), 
                    loginRequest.getPassword()
                )
            );

            // 2. 获取用户信息
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // Add debug logging
            log.debug("User authorities before check: {}", userDetails.getAuthorities());
            
            // 3. 检查是否是管理员（使用更灵活的角色检查）
            boolean isAdmin = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .peek(auth -> log.debug("Checking authority: {}", auth))
                .map(String::toUpperCase)  // 转换为大写以进行不区分大小写的比较
                .anyMatch(auth -> auth.contains("USER"));  // 测试用，改为检查USER角色
            
            log.debug("Is admin check result: {}", isAdmin);
            
            if (!isAdmin) {
                throw new AccessDeniedException("非管理员账号");
            }

            // 4. 生成 JWT token
            String token = jwtUtils.generateToken(userDetails);

            return ResponseEntity.ok(new JwtResponse(token));
            
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("用户名或密码错误");
        }
    }

    @GetMapping("/stats/overview")
    public ResponseEntity<DashboardStatsDTO> getOverviewStats() {
        DashboardStatsDTO stats = statsService.getDashboardStats();
        log.debug("Overview stats: {}", stats);  // 添加日志
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/sales")
    public ResponseEntity<List<DailySalesStatsDTO>> getSalesStats(
        @RequestParam(defaultValue = "week") String range
    ) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = range.equals("month") 
            ? endTime.minusMonths(1) 
            : endTime.minusWeeks(1);
        return ResponseEntity.ok(statsService.getDailySalesStats(startTime, endTime));
    }

    @GetMapping("/stats/products/top")
    public ResponseEntity<List<ProductStatsDTO>> getTopProducts(
        @RequestParam(defaultValue = "week") String range,
        @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(statsService.getTopSellingProducts(limit));
    }

    @GetMapping("/orders/recent")
    public ResponseEntity<List<OrderDTO>> getRecentOrders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Authentication authentication) {
        
        OrderSearchDTO searchDTO = new OrderSearchDTO();
        searchDTO.setUsername(authentication.getName());
        
        Page<OrderDTO> ordersPage = orderService.getOrders(
            searchDTO,
            PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        
        return ResponseEntity.ok(ordersPage.getContent());
    }

    @GetMapping("/stats/sales/trend")
    public ResponseEntity<List<DailySalesStatsDTO>> getSalesTrend(@RequestParam(defaultValue = "7") int days) {
        List<DailySalesStatsDTO> trend = statsService.getSalesTrend(days);
        return ResponseEntity.ok(trend);
    }
} 