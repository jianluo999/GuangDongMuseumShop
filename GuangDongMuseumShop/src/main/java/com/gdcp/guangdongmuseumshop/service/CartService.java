package com.gdcp.guangdongmuseumshop.service;

import com.gdcp.guangdongmuseumshop.dto.CartItemDTO;
import com.gdcp.guangdongmuseumshop.vo.CartVO;
import com.gdcp.guangdongmuseumshop.vo.CartDetailVO;
import com.gdcp.guangdongmuseumshop.dto.CartDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CartService {
    CartItemDTO addToCart(Long userId, CartItemDTO cartItemDTO);
    void removeFromCart(Long userId, Long productId);
    CartItemDTO updateQuantity(Long userId, Long itemId, Integer quantity);
    void updateSelected(Long userId, Long itemId, Boolean selected);
    List<CartItemDTO> getCartItems(Long userId);
    void clearCart(Long userId);
    List<CartItemDTO> getSelectedItems(Long userId);
    Page<CartDTO> getAdminCartList(Pageable pageable, String userId, String status);
    CartDetailVO getAdminCartDetail(Long id);
    void deleteCart(Long id);
    void updateCartStatus(Long id, String status);
} 