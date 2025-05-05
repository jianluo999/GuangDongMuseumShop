package com.gdcp.guangdongmuseumshop.repository;

import com.gdcp.guangdongmuseumshop.entity.Review;
import com.gdcp.guangdongmuseumshop.dto.stats.ReviewStatsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {
    Page<Review> findByProductId(Long productId, Pageable pageable);
    Page<Review> findByUserId(Long userId, Pageable pageable);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = ?1")
    Long getReviewCountByProductId(Long productId);
    
    boolean existsByUserIdAndOrderId(Long userId, Long orderId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = ?1 AND r.rating = ?2")
    int countByProductIdAndRating(Long productId, Integer rating);
    
    @Query("SELECT NEW com.gdcp.guangdongmuseumshop.dto.stats.ReviewStatsDTO(" +
            "p.id, p.name, AVG(r.rating), COUNT(r), " +
            "SUM(CASE WHEN r.rating = 5 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.rating = 4 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.rating = 3 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.rating = 2 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.rating = 1 THEN 1 ELSE 0 END)) " +
            "FROM Review r JOIN r.product p " +
            "WHERE p.id = ?1 GROUP BY p.id, p.name")
    ReviewStatsDTO getReviewStats(Long productId);
    
    @Query("SELECT AVG(r.rating) FROM Review r")
    Double getOverallAverageRating();
    
    @Query("SELECT COUNT(r) * 100.0 / (SELECT COUNT(r2) FROM Review r2) " +
           "FROM Review r WHERE r.rating >= 4")
    Double getOverallPositiveRate();
    
    @Query("SELECT NEW com.gdcp.guangdongmuseumshop.dto.stats.ReviewStatsDTO(" +
            "p.id, p.name, AVG(r.rating), COUNT(r), " +
            "SUM(CASE WHEN r.rating = 5 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.rating = 4 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.rating = 3 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.rating = 2 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.rating = 1 THEN 1 ELSE 0 END)) " +
            "FROM Review r RIGHT JOIN r.product p GROUP BY p.id, p.name")
    List<ReviewStatsDTO> getAllProductsReviewStats(Pageable pageable);
    
    @Query("SELECT NEW com.gdcp.guangdongmuseumshop.dto.stats.ReviewStatsDTO(" +
            "p.id, p.name, AVG(r.rating), COUNT(r), " +
            "SUM(CASE WHEN r.rating = 5 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.rating = 4 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.rating = 3 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.rating = 2 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.rating = 1 THEN 1 ELSE 0 END)) " +
            "FROM Review r RIGHT JOIN r.product p WHERE p.category.id = ?1 GROUP BY p.id, p.name")
    List<ReviewStatsDTO> getReviewStatsByCategory(Long categoryId, Pageable pageable);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    Page<Review> findByProductIdAndVisibleTrue(Long productId, Pageable pageable);
    
    Long countByProductIdAndVisibleTrue(Long productId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.visible = true")
    Double getAverageRatingByProductId(@Param("productId") Long productId);
} 