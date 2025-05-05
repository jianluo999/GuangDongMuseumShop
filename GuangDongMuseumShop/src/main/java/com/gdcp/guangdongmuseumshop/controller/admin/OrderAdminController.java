package com.gdcp.guangdongmuseumshop.controller.admin;

import com.gdcp.guangdongmuseumshop.common.Result;
import com.gdcp.guangdongmuseumshop.dto.OrderDTO;
import com.gdcp.guangdongmuseumshop.dto.OrderSearchDTO;
import com.gdcp.guangdongmuseumshop.dto.ShipOrderRequest;
import com.gdcp.guangdongmuseumshop.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ROLE_USER', 'ROLE_ADMIN','STAFF')")
@RequiredArgsConstructor
@Slf4j
public class OrderAdminController {
    private final OrderService orderService;

    @GetMapping
    public Result<?> getOrders(OrderSearchDTO searchDTO, Pageable pageable) {
        log.info("开始查询订单列表");
        log.debug("查询参数: searchDTO={}, pageable={}", searchDTO, pageable);
        
        Page<OrderDTO> orders = orderService.getOrders(searchDTO, pageable);
        
        log.info("查询到订单数量: {}", orders.getTotalElements());
        log.debug("订单列表: {}", orders.getContent());
        
        return Result.success(orders);
    }

    @PutMapping("/{id}/ship")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ROLE_USER', 'ROLE_ADMIN','STAFF')")
    public Result<?> shipOrder(@PathVariable("id") Long orderId, @RequestBody ShipOrderRequest request) {
        log.info("收到发货请求，订单ID: {}, 物流信息: {}", orderId, request);
        try {
            orderService.shipOrder(orderId, request.getShippingCompany(), request.getTrackingNo());
            return Result.success(null);
        } catch (Exception e) {
            log.error("发货失败: orderId={}", orderId, e);
            return Result.error(500, "发货失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<OrderDTO> getOrderDetail(@PathVariable Long id) {
        log.info("开始获取订单详情, id: {}", id);
        try {
            OrderDTO order = orderService.getOrderById(id);
            log.info("获取订单详情成功");
            return Result.success(order);
        } catch (Exception e) {
            log.error("获取订单详情失败: {}", e.getMessage(), e);
            return Result.error(500, "获取订单详情失败");
        }
    }

    @GetMapping("/latest")
    public Result<?> getRecentOrders() {
        log.info("开始获取最新订单");
        try {
            Page<OrderDTO> recentOrders = orderService.getOrders(
                new OrderSearchDTO(), 
                Pageable.ofSize(10).withPage(0)
            );
            return Result.success(recentOrders.getContent());
        } catch (Exception e) {
            log.error("获取最新订单失败: {}", e.getMessage(), e);
            return Result.error(500, "获取最新订单失败");
        }
    }
} 