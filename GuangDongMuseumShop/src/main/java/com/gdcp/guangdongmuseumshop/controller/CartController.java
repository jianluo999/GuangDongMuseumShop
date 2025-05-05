package com.gdcp.guangdongmuseumshop.controller;

import com.gdcp.guangdongmuseumshop.common.Result;
import com.gdcp.guangdongmuseumshop.dto.CartItemDTO;
import com.gdcp.guangdongmuseumshop.dto.UpdateCartItemQuantityDTO;
import com.gdcp.guangdongmuseumshop.service.CartService;
import com.gdcp.guangdongmuseumshop.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<CartItemDTO> addToCart(
            @Valid @RequestBody CartItemDTO cartItemDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        log.debug("添加商品到购物车: userId={}, cartItemDTO={}", userId, cartItemDTO);
        return Result.success(cartService.addToCart(userId, cartItemDTO));
    }

    @DeleteMapping("/items")
    @PreAuthorize("isAuthenticated()")
    public Result<?> removeFromCart(
            @RequestBody CartItemDTO cartItemDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        log.debug("从购物车移除商品: userId={}, itemId={}", userId, cartItemDTO.getItemId());
        cartService.removeFromCart(userId, cartItemDTO.getItemId());
        return Result.success("商品已从购物车移除");
    }

    @PutMapping("/items/quantity")
    @PreAuthorize("isAuthenticated()")
    public Result<CartItemDTO> updateQuantity(
            @Valid @RequestBody UpdateCartItemQuantityDTO updateDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        log.debug("更新购物车商品数量: userId={}, itemId={}, quantity={}", 
                userId, updateDTO.getItemId(), updateDTO.getQuantity());
        return Result.success(cartService.updateQuantity(userId, updateDTO.getItemId(), updateDTO.getQuantity()));
    }

    @PutMapping("/items/selected")
    @PreAuthorize("isAuthenticated()")
    public Result<?> updateSelected(
            @Valid @RequestBody CartItemDTO cartItemDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        log.debug("更新购物车商品选中状态: userId={}, itemId={}, selected={}", 
                userId, cartItemDTO.getItemId(), cartItemDTO.getSelected());
        cartService.updateSelected(userId, cartItemDTO.getItemId(), cartItemDTO.getSelected());
        return Result.success("更新成功");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<List<CartItemDTO>> getCartItems(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        log.debug("获取购物车商品列表: userId={}", userId);
        return Result.success(cartService.getCartItems(userId));
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public Result<?> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        log.debug("清空购物车: userId={}", userId);
        cartService.clearCart(userId);
        return Result.success("购物车已清空");
    }

    @GetMapping("/selected")
    @PreAuthorize("isAuthenticated()")
    public Result<List<CartItemDTO>> getSelectedItems(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        log.debug("获取购物车已选中商品: userId={}", userId);
        return Result.success(cartService.getSelectedItems(userId));
    }

    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        if (userDetails instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) userDetails).getId();
        }
        log.error("无法获取用户ID，UserDetails类型不正确: {}", userDetails.getClass());
        throw new IllegalStateException("无法获取用户ID");
    }
} 