package com.gdcp.guangdongmuseumshop.controller.admin;

import com.gdcp.guangdongmuseumshop.common.Result;
import com.gdcp.guangdongmuseumshop.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.gdcp.guangdongmuseumshop.dto.ReviewDTO;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ReviewAdminController {

    private final ReviewService reviewService;
    private static final Logger log = LoggerFactory.getLogger(ReviewAdminController.class);

    @GetMapping
    public Result<Page<ReviewDTO>> getReviewList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer rating
    ) {
        log.info("当前用户: {}", SecurityContextHolder.getContext().getAuthentication().getName());
        log.info("用户角色: {}", SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        
        log.info("接收到评论列表查询请求: page={}, size={}, productName={}, username={}, rating={}", 
            page, size, productName, username, rating);
        
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<ReviewDTO> reviews = reviewService.getAdminReviewList(pageable, productName, username, rating);
        
        log.info("查询到评论数量: {}", reviews.getTotalElements());
        log.info("返回评论数据: {}", reviews.getContent());
        
        return Result.success(reviews);
    }

    @PutMapping("/{id}/visible")
    public Result<Void> updateReviewVisible(@PathVariable Long id, @RequestBody Boolean visible) {
        reviewService.updateReviewVisible(id, visible);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return Result.success(null);
    }
} 