package com.gdcp.guangdongmuseumshop.controller.admin;

import com.gdcp.guangdongmuseumshop.common.Result;
import com.gdcp.guangdongmuseumshop.service.CartService;
import com.gdcp.guangdongmuseumshop.vo.CartDetailVO;
import com.gdcp.guangdongmuseumshop.dto.CartStatusUpdateDTO;
import com.gdcp.guangdongmuseumshop.dto.CartDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/carts")
@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ROLE_USER')")
@RequiredArgsConstructor
@Slf4j
public class CartAdminController {
    private final CartService cartService;
    
    @GetMapping
    public Result<?> getCartList(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String userId,
        @RequestParam(required = false) String status
    ) {
        log.info("获取购物车列表: page={}, size={}, userId={}, status={}", page, size, userId, status);
        
        try {
            Page<CartDTO> result = cartService.getAdminCartList(PageRequest.of(page, size), userId, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取购物车列表失败:", e);
            return Result.error(500, "获取购物车列表失败");
        }
    }
    
    @GetMapping("/{id}")
    public Result<CartDetailVO> getCartDetail(@PathVariable Long id) {
        return Result.success(cartService.getAdminCartDetail(id));
    }
    
    @DeleteMapping("/{id}")
    public Result<String> deleteCart(@PathVariable Long id) {
        cartService.deleteCart(id);
        return Result.success("删除成功");
    }
    
    @PutMapping("/{id}/status")
    public Result<String> updateCartStatus(
        @PathVariable Long id,
        @RequestBody CartStatusUpdateDTO dto
    ) {
        cartService.updateCartStatus(id, dto.getStatus());
        return Result.success("状态更新成功");
    }
} 