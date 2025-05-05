package com.gdcp.guangdongmuseumshop.repository;

import com.gdcp.guangdongmuseumshop.entity.Cart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long>, JpaSpecificationExecutor<Cart> {
    Optional<Cart> findByUser_Id(Long userId);
    Page<Cart> findByUser_Id(Long userId, Pageable pageable);
    Page<Cart> findByStatus(String status, Pageable pageable);
    Page<Cart> findByUser_IdAndStatus(Long userId, String status, Pageable pageable);
} 