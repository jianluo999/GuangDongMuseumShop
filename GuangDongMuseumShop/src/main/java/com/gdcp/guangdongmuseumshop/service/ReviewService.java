package com.gdcp.guangdongmuseumshop.service;

import com.gdcp.guangdongmuseumshop.dto.ReviewDTO;
import com.gdcp.guangdongmuseumshop.dto.stats.ReviewStatsDTO;
import com.gdcp.guangdongmuseumshop.dto.CreateReviewRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {
    ReviewDTO create(Long userId, ReviewDTO reviewDTO);
    void delete(Long userId, Long reviewId);
    ReviewDTO update(Long userId, Long reviewId, ReviewDTO reviewDTO);
    ReviewDTO getById(Long reviewId);
    Page<ReviewDTO> getByProduct(Long productId, Pageable pageable);
    Page<ReviewDTO> getByUser(Long userId, Pageable pageable);
    Double getAverageRating(Long productId);
    Long getReviewCount(Long productId);
    ReviewStatsDTO getReviewStats(Long productId);
    List<ReviewStatsDTO> getReviewStatsByCategory(Long categoryId, Pageable pageable);
    List<ReviewStatsDTO> getAllProductsReviewStats(Pageable pageable);
    Page<ReviewDTO> getAdminReviewList(Pageable pageable, String productName, String username, Integer rating);
    void updateReviewVisible(Long id, Boolean visible);
    void deleteReview(Long id);
    boolean canReview(Long productId);
    ReviewDTO createReview(ReviewDTO reviewDTO);
    boolean hasReviewed(Long productId);
    Page<ReviewDTO> getProductReviews(Long productId, Pageable pageable);
    boolean canUserReview(Long productId, String username);
    boolean canUserReview(Long productId, String username, Long orderId);
    ReviewDTO createReview(Long productId, String username, CreateReviewRequest request);
} 