package com.gdcp.guangdongmuseumshop.controller;

import com.gdcp.guangdongmuseumshop.common.Result;
import com.gdcp.guangdongmuseumshop.dto.CreateOrderRequest;
import com.gdcp.guangdongmuseumshop.dto.OrderDTO;
import com.gdcp.guangdongmuseumshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;
import com.gdcp.guangdongmuseumshop.dto.PageDTO;
import org.springframework.web.bind.annotation.PathVariable;
import com.gdcp.guangdongmuseumshop.dto.OrderSearchDTO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5174")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<OrderDTO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("========== 开始创建订单 ==========");
        log.info("收到创建订单请求，请求内容: {}", request);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        log.info("当前用户: {}", username);
        try {
            OrderDTO order = orderService.createOrder(username, request);
            log.info("订单创建成功，订单信息: {}", order);
            return Result.success(order);
        } catch (Exception e) {
            log.error("========== 创建订单失败 ==========");
            log.error("请求数据: {}", request);
            log.error("错误详情: ", e);
            return Result.error(500, "创建订单失败: " + e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageDTO<OrderDTO>> getOrders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String status,
        Authentication authentication) {
        
        OrderSearchDTO searchDTO = new OrderSearchDTO();
        searchDTO.setUsername(authentication.getName());
        searchDTO.setStatus(status);
        
        Page<OrderDTO> orders = orderService.getOrders(
            searchDTO,
            PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        
        return Result.success(PageDTO.from(orders));
    }

    @PostMapping("/{id}/pay")
    public Result<?> payOrder(@PathVariable("id") Long orderId) {
        log.info("收到支付请求，订单ID: {}", orderId);
        try {
            orderService.payOrder(orderId);
            log.info("订单支付成功");
            return Result.success(null);
        } catch (Exception e) {
            log.error("支付订单失败: orderId={}", orderId, e);
            return Result.error(500, "支付失败: " + e.getMessage());
        }
    }

    @GetMapping("/{orderNo}")
    public Result<OrderDTO> getOrderDetail(@PathVariable String orderNo) {
        try {
            OrderDTO orderDTO = orderService.getOrderDetail(orderNo);
            return Result.success(orderDTO);
        } catch (Exception e) {
            log.error("获取订单详情失败: {}", e.getMessage(), e);
            return Result.error(500, "获取订单详情失败");
        }
    }

    @GetMapping("/list")
    public Result<List<OrderDTO>> getUserOrders(
        @RequestParam(required = false) String status,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            List<OrderDTO> orders = orderService.getUserOrders(
                userDetails.getUsername(), 
                status
            );
            return Result.success(orders);
        } catch (Exception e) {
            log.error("获取订单列表失败: {}", e.getMessage(), e);
            return Result.error(500, "获取订单列表失败");
        }
    }

    @PostMapping("/{id}/cancel")
    public Result<?> cancelOrder(@PathVariable("id") Long orderId) {
        log.info("收到取消订单请求，订单ID: {}", orderId);
        try {
            orderService.cancelOrder(orderId);
            log.info("订单取消成功");
            return Result.success(null);
        } catch (Exception e) {
            log.error("取消订单失败: orderId={}", orderId, e);
            return Result.error(500, "取消订单失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("isAuthenticated()")
    public Result<?> confirmReceive(@PathVariable("id") Long orderId, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            orderService.confirmReceive(orderId, userDetails.getUsername());
            return Result.success(null);
        } catch (Exception e) {
            log.error("确认收货失败: orderId={}, username={}", orderId, userDetails.getUsername(), e);
            return Result.error(500, "确认收货失败: " + e.getMessage());
        }
    }
} 