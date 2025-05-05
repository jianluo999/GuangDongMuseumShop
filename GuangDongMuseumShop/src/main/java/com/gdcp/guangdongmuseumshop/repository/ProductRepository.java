package com.gdcp.guangdongmuseumshop.repository;

import com.gdcp.guangdongmuseumshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findByCategoryIdAndEnabledTrue(Long categoryId, Pageable pageable);
    
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    long countByEnabledTrue();

    @Query(value = "SELECT * FROM products WHERE enabled = true ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Product> findRandomEnabledProducts(@Param("limit") int limit);
} 