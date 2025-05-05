package com.gdcp.guangdongmuseumshop.repository;


import com.gdcp.guangdongmuseumshop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.user.id = ?1 AND ci.selected = true")
    List<CartItem> findSelectedItems(Long userId);
    
    List<CartItem> findByCartId(Long cartId);
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
    void deleteByCartIdAndProductId(Long cartId, Long productId);
    List<CartItem> findByCartIdAndSelected(Long cartId, Boolean selected);
} 