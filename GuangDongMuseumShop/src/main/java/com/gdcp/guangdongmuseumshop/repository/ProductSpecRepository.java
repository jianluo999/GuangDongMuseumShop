package com.gdcp.guangdongmuseumshop.repository;

import com.gdcp.guangdongmuseumshop.entity.ProductSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductSpecRepository extends JpaRepository<ProductSpec, Long> {
    List<ProductSpec> findByProductId(Long productId);
    List<ProductSpec> findByProductIdOrderBySortOrder(Long productId);
    void deleteByProductId(Long productId);
} 