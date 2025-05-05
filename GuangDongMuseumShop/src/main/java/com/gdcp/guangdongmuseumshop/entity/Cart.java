package com.gdcp.guangdongmuseumshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.gdcp.guangdongmuseumshop.enums.CartStatus;

@Data
@Entity
@Table(name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    private CartStatus status = CartStatus.NORMAL;
    
    @CreationTimestamp
    private LocalDateTime createTime;
    
    @UpdateTimestamp
    private LocalDateTime updateTime;

    // 计算总金额
    public BigDecimal getTotalAmount() {
        return items.stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 获取商品总数
    public int getItemCount() {
        return items.stream()
            .mapToInt(CartItem::getQuantity)
            .sum();
    }
} 