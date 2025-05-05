package com.gdcp.guangdongmuseumshop.repository;


import com.gdcp.guangdongmuseumshop.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductIdOrderBySort(Long productId);
    void deleteByProductId(Long productId);
} 