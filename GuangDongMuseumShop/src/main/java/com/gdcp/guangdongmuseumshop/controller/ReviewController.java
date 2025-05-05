package com.gdcp.guangdongmuseumshop.controller;

import com.gdcp.guangdongmuseumshop.common.Result;
import com.gdcp.guangdongmuseumshop.dto.ReviewDTO;
import com.gdcp.guangdongmuseumshop.dto.stats.ReviewStatsDTO;
import com.gdcp.guangdongmuseumshop.security.SecurityUtils;
import com.gdcp.guangdongmuseumshop.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.gdcp.guangdongmuseumshop.dto.ReviewRequest;
import com.gdcp.guangdongmuseumshop.service.ProductService;
import com.gdcp.guangdongmuseumshop.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import com.gdcp.guangdongmuseumshop.dto.CreateReviewRequest;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;
    private final ProductService productService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<ReviewDTO> create(@Valid @RequestBody ReviewDTO reviewDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(reviewService.create(userId, reviewDTO));
    }

    @DeleteMapping("/reviews/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<?> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        reviewService.delete(userId, id);
        return Result.success("评价删除成功");
    }

    @PutMapping("/reviews/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<ReviewDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ReviewDTO reviewDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(reviewService.update(userId, id, reviewDTO));
    }

    @GetMapping("/reviews/{id}")
    public Result<ReviewDTO> getById(@PathVariable Long id) {
        return Result.success(reviewService.getById(id));
    }

    @GetMapping("/products/{productId}/reviews")
    public Result<?> getProductReviews(@PathVariable Long productId, Pageable pageable) {
        log.info("获取商品评论列表, productId={}, pageable={}", productId, pageable);
        try {
            Page<ReviewDTO> reviews = reviewService.getProductReviews(productId, pageable);
            log.debug("获取到 {} 条评论", reviews.getContent().size());
            return Result.success(reviews);
        } catch (Exception e) {
            log.error("获取商品评论失败", e);
            return Result.error(500, "获取评论失败");
        }
    }

    @GetMapping("/users/me/reviews")
    @PreAuthorize("isAuthenticated()")
    public Result<Page<ReviewDTO>> getMyReviews(
            @PageableDefault(size = 10) Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(reviewService.getByUser(userId, pageable));
    }

    @GetMapping("/products/{productId}/review-stats")
    public Result<?> getReviewStats(@PathVariable Long productId) {
        log.info("获取商品评论统计, productId={}", productId);
        try {
            // 先检查商品是否存在
            ProductDTO product = productService.getProductById(productId);
            if (product == null) {
                return Result.error(404, "商品不存在");
            }
            
            ReviewStatsDTO stats = reviewService.getReviewStats(productId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取商品评论统计失败: ", e);
            return Result.error(500, "获取评论统计失败");
        }
    }

    @GetMapping("/products/{productId}/review-status")
    @PreAuthorize("isAuthenticated()")
    public Result<?> checkReviewStatus(
            @PathVariable Long productId,
            @RequestParam(required = false) Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("检查评价权限: productId={}, orderId={}, username={}", productId, orderId, userDetails.getUsername());
        
        boolean canReview;
        if (orderId != null) {
            log.debug("使用订单ID检查评价权限");
            canReview = reviewService.canUserReview(productId, userDetails.getUsername(), orderId);
        } else {
            log.debug("不使用订单ID检查评价权限");
            canReview = reviewService.canUserReview(productId, userDetails.getUsername());
        }
        
        log.info("评价权限检查结果: {}", canReview);
        return Result.success(Map.of("canReview", canReview));
    }

    @PostMapping("/products/{productId}/reviews")
    @PreAuthorize("isAuthenticated()")
    public Result<?> createReview(
            @PathVariable Long productId,
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("开始创建商品评论: productId={}, username={}, orderId={}", 
                productId, userDetails.getUsername(), request.getOrderId());
        
        try {
            log.debug("评论请求详情: rating={}, content={}, anonymous={}, images={}",
                    request.getRating(), request.getContent(), 
                    request.isAnonymous(), request.getImages());
                    
            ReviewDTO review = reviewService.createReview(
                    productId, 
                    userDetails.getUsername(), 
                    request);
                    
            log.info("评论创建成功: reviewId={}", review.getId());
            return Result.success(review);
        } catch (Exception e) {
            log.error("创建评论失败: productId={}, username={}, error={}", 
                    productId, userDetails.getUsername(), e.getMessage(), e);
            return Result.error(500, "创建评论失败: " + e.getMessage());
        }
    }
} 