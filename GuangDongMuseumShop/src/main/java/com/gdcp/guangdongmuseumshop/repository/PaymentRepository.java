package com.gdcp.guangdongmuseumshop.repository;

import com.gdcp.guangdongmuseumshop.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentNo(String paymentNo);
    Optional<Payment> findByOrderId(Long orderId);
    
    @Query("SELECT p FROM Payment p WHERE p.order.id = ?1 AND p.order.user.id = ?2")
    Optional<Payment> findByOrderIdAndUserId(Long orderId, Long userId);
} 