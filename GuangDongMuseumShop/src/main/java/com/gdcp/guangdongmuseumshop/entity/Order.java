package com.gdcp.guangdongmuseumshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.gdcp.guangdongmuseumshop.enums.OrderStatus;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orderNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    private LocalDateTime paymentTime;

    @Column(name = "shipping_time")
    private LocalDateTime shippingTime;

    @Column(name = "completion_time")
    private LocalDateTime completionTime;

    private String receiver;
    private String phone;
    private String address;

    @Column(name = "shipping_name")
    private String shippingName;
    
    @Column(name = "shipping_phone")
    private String shippingPhone;
    
    @Column(name = "shipping_address")
    private String shippingAddress;

    @Column(name = "shipping_company")
    private String shippingCompany;

    @Column(name = "tracking_no")
    private String trackingNo;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<OrderItem> orderItems = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void setOrderItems(List<OrderItem> items) {
        this.orderItems = items;
        items.forEach(item -> item.setOrder(this));
    }
} 