package com.gdcp.guangdongmuseumshop.service.impl;

import com.gdcp.guangdongmuseumshop.dto.ReviewDTO;
import com.gdcp.guangdongmuseumshop.exception.ReviewException;
import com.gdcp.guangdongmuseumshop.entity.Product;
import com.gdcp.guangdongmuseumshop.entity.Review;
import com.gdcp.guangdongmuseumshop.entity.Order;

import com.gdcp.guangdongmuseumshop.entity.User;
import com.gdcp.guangdongmuseumshop.repository.OrderRepository;
import com.gdcp.guangdongmuseumshop.repository.ProductRepository;
import com.gdcp.guangdongmuseumshop.repository.ReviewRepository;
import com.gdcp.guangdongmuseumshop.repository.UserRepository;
import com.gdcp.guangdongmuseumshop.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gdcp.guangdongmuseumshop.enums.OrderStatus;
import com.gdcp.guangdongmuseumshop.dto.stats.ReviewStatsDTO;
import java.util.List;
import java.util.stream.Collectors;

import com.gdcp.guangdongmuseumshop.config.DefaultsConfig;
import com.gdcp.guangdongmuseumshop.entity.ReviewImage;
import com.gdcp.guangdongmuseumshop.exception.ResourceNotFoundException;
import com.gdcp.guangdongmuseumshop.mapper.ReviewMapper;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gdcp.guangdongmuseumshop.security.SecurityUtils;
import org.springframework.data.redis.RedisConnectionFailureException;
import com.gdcp.guangdongmuseumshop.exception.UserException;
import com.gdcp.guangdongmuseumshop.exception.ProductException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.TimeUnit;
import com.gdcp.guangdongmuseumshop.dto.CreateReviewRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final DefaultsConfig defaultsConfig;
    private final ReviewMapper reviewMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    @CacheEvict(value = {"productRating", "reviewCount"}, key = "#reviewDTO.productId")
    public ReviewDTO create(Long userId, ReviewDTO reviewDTO) {
        // 检查是否已经评价过
        if (reviewRepository.existsByUserIdAndOrderId(userId, reviewDTO.getOrderId())) {
            throw new ReviewException("该订单已经评价过了");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ReviewException("用户不存在"));
        
        Product product = productRepository.findById(reviewDTO.getProductId())
                .orElseThrow(() -> new ReviewException("商品不存在"));
        
        Order order = orderRepository.findById(reviewDTO.getOrderId())
                .orElseThrow(() -> new ReviewException("订单不存在"));

        // 检查订单状态是否已完成
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new ReviewException("订单未完成，不能评价");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setOrder(order);
        review.setRating(reviewDTO.getRating());
        review.setContent(reviewDTO.getContent());
        
        // 保存评价图片
        if (reviewDTO.getImages() != null && !reviewDTO.getImages().isEmpty()) {
            final Review finalReview = review;
            List<ReviewImage> reviewImages = reviewDTO.getImages().stream()
                .map(url -> {
                    ReviewImage image = new ReviewImage();
                    image.setImageUrl(url);
                    image.setReview(finalReview);
                    return image;
                })
                .collect(Collectors.toList());
            review.setImages(reviewImages);
        }
        
        review.setAnonymous(reviewDTO.getAnonymous() != null && reviewDTO.getAnonymous());

        review = reviewRepository.save(review);
        return convertToDTO(review);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"productRating", "reviewCount"}, key = "#review.product.id")
    public void delete(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException("评价不存在"));

        if (!review.getUser().getId().equals(userId)) {
            throw new ReviewException("无权删除此评价");
        }

        reviewRepository.delete(review);
    }

    @Override
    @Transactional
    public ReviewDTO update(Long userId, Long reviewId, ReviewDTO reviewDTO) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException("评价不存在"));

        if (!review.getUser().getId().equals(userId)) {
            throw new ReviewException("无权修改此评价");
        }

        review.setRating(reviewDTO.getRating());
        review.setContent(reviewDTO.getContent());
        
        if (reviewDTO.getImages() != null) {
            final Review finalReview = review;
            List<ReviewImage> reviewImages = reviewDTO.getImages().stream()
                .map(url -> {
                    ReviewImage image = new ReviewImage();
                    image.setImageUrl(url);
                    image.setReview(finalReview);
                    return image;
                })
                .collect(Collectors.toList());
            review.setImages(reviewImages);
        }
        
        review.setAnonymous(reviewDTO.getAnonymous() != null && reviewDTO.getAnonymous());

        review = reviewRepository.save(review);
        return convertToDTO(review);
    }

    @Override
    public ReviewDTO getById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException("评价不存在"));
        return convertToDTO(review);
    }

    @Override
    public Page<ReviewDTO> getByProduct(Long productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<ReviewDTO> getByUser(Long userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Cacheable(value = "productRating", key = "#productId")
    public Double getAverageRating(Long productId) {
        return reviewRepository.getAverageRatingByProductId(productId);
    }

    @Override
    @Cacheable(value = "reviewCount", key = "#productId")
    public Long getReviewCount(Long productId) {
        return reviewRepository.getReviewCountByProductId(productId);
    }

    @Override
    @Cacheable(value = "reviewStats", key = "#productId", unless = "#result == null")
    public ReviewStatsDTO getReviewStats(Long productId) {
        try {
            // 尝试从缓存获取
            String cacheKey = "review:stats:" + productId;
            ReviewStatsDTO cachedStats = (ReviewStatsDTO) redisTemplate.opsForValue().get(cacheKey);
            if (cachedStats != null) {
                return cachedStats;
            }
            
            // 缓存未命中,计算统计信息
            ReviewStatsDTO stats = calculateReviewStats(productId);
            
            try {
                // 尝试写入缓存,设置过期时间
                redisTemplate.opsForValue().set(cacheKey, stats, 30, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("Redis缓存写入失败: {}", e.getMessage());
                // 缓存写入失败不影响主流程
            }
            
            return stats;
        } catch (Exception e) {
            log.error("获取评论统计信息失败: {}", e.getMessage());
            // 发生异常时返回空统计信息而不是抛出异常
            return ReviewStatsDTO.builder()
                    .productId(productId)
                    .build();
        }
    }

    // 添加一个用于直接计算统计信息的方法
    private ReviewStatsDTO calculateReviewStats(Long productId) {
        int fiveStarCount = reviewRepository.countByProductIdAndRating(productId, 5);
        int fourStarCount = reviewRepository.countByProductIdAndRating(productId, 4);
        int threeStarCount = reviewRepository.countByProductIdAndRating(productId, 3);
        int twoStarCount = reviewRepository.countByProductIdAndRating(productId, 2);
        int oneStarCount = reviewRepository.countByProductIdAndRating(productId, 1);
        
        long totalCount = fiveStarCount + fourStarCount + threeStarCount + twoStarCount + oneStarCount;
        
        double averageRating = totalCount > 0 ? 
            (5.0 * fiveStarCount + 4.0 * fourStarCount + 3.0 * threeStarCount + 
             2.0 * twoStarCount + 1.0 * oneStarCount) / totalCount : 0.0;
            
        return ReviewStatsDTO.builder()
                .productId(productId)
                .averageRating(averageRating)
                .totalCount(totalCount)
                .fiveStarCount((long) fiveStarCount)
                .fourStarCount((long) fourStarCount)
                .threeStarCount((long) threeStarCount)
                .twoStarCount((long) twoStarCount)
                .oneStarCount((long) oneStarCount)
                .build();
    }

    @Override
    public List<ReviewStatsDTO> getAllProductsReviewStats(Pageable pageable) {
        return reviewRepository.getAllProductsReviewStats(pageable);
    }

    @Override
    public List<ReviewStatsDTO> getReviewStatsByCategory(Long categoryId, Pageable pageable) {
        return reviewRepository.getReviewStatsByCategory(categoryId, pageable);
    }

    @Override
    public Page<ReviewDTO> getAdminReviewList(Pageable pageable, String productName, String username, Integer rating) {
        // 添加日志
        log.info("查询参数: productName={}, username={}, rating={}, page={}, size={}", 
            productName, username, rating, pageable.getPageNumber(), pageable.getPageSize());

        Specification<Review> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(productName)) {
                predicates.add(cb.like(root.get("product").get("name"), "%" + productName + "%"));
            }
            if (StringUtils.hasText(username)) {
                predicates.add(cb.like(root.get("user").get("username"), "%" + username + "%"));
            }
            if (rating != null) {
                predicates.add(cb.equal(root.get("rating"), rating));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<Review> reviews = reviewRepository.findAll(spec, pageable);
        log.info("查询到评论数量: {}", reviews.getTotalElements());
        
        Page<ReviewDTO> dtoPage = reviews.map(review -> {
            ReviewDTO dto = reviewMapper.toDTO(review);
            dto.setImages(reviewMapper.mapImagesToStrings(review.getImages()));  // 使用mapper方法转换图片列表
            log.info("评论详情: id={}, username={}", dto.getId(), dto.getUsername());
            return dto;
        });
        
        return dtoPage;
    }

    @Override
    @Transactional
    public void updateReviewVisible(Long id, Boolean visible) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("评价不存在"));
        review.setVisible(visible);
        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("评价不存在");
        }
        reviewRepository.deleteById(id);
    }

    @Override
    public boolean canReview(Long productId) {
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserException("用户不存在"));
            
        // 检查是否已经评价过
        boolean hasReviewed = reviewRepository.existsByUserIdAndProductId(user.getId(), productId);
        if (hasReviewed) {
            return false;
        }

        // 检查是否有已完成的订单包含该商品
        return orderRepository.existsByUserIdAndProductIdAndStatus(
            user.getId(), 
            productId, 
            OrderStatus.COMPLETED
        );
    }

    @Override
    @Transactional
    public ReviewDTO createReview(ReviewDTO reviewDTO) {
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserException("用户不存在"));

        Product product = productRepository.findById(reviewDTO.getProductId())
            .orElseThrow(() -> new ProductException("商品不存在"));

        // 检查是否可以评价
        if (!canReview(reviewDTO.getProductId())) {
            throw new ReviewException("无法评价该商品");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(reviewDTO.getRating());
        review.setContent(reviewDTO.getContent());
        review.setAnonymous(reviewDTO.getAnonymous());

        // 保存评价图片
        if (reviewDTO.getImages() != null && !reviewDTO.getImages().isEmpty()) {
            final Review finalReview = review;
            List<ReviewImage> reviewImages = reviewDTO.getImages().stream()
                .map(url -> {
                    ReviewImage image = new ReviewImage();
                    image.setImageUrl(url);
                    image.setReview(finalReview);
                    return image;
                })
                .collect(Collectors.toList());
            review.setImages(reviewImages);
        }

        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toDTO(savedReview);
    }

    @Override
    public boolean hasReviewed(Long productId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return reviewRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    public Page<ReviewDTO> getProductReviews(Long productId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByProductIdAndVisibleTrue(productId, pageable);
        return reviews.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserReview(Long productId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
                
        // 检查用户是否已购买且确认收货
        boolean hasPurchased = orderRepository.existsByUserIdAndProductIdAndStatus(
                user.getId(), productId, OrderStatus.COMPLETED);
                
        // 检查用户是否已经评价过
        boolean hasReviewed = reviewRepository.existsByUserIdAndProductId(
                user.getId(), productId);
                
        return hasPurchased && !hasReviewed;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserReview(Long productId, String username, Long orderId) {
        log.info("检查用户评价权限: productId={}, username={}, orderId={}", productId, username, orderId);
        
        // 检查用户是否存在
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("用户不存在: {}", username);
                    return new RuntimeException("用户不存在");
                });
        
        // 检查订单是否存在且状态为已完成
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("订单不存在: {}", orderId);
                    return new RuntimeException("订单不存在");
                });
        
        log.debug("订单状态: {}", order.getStatus());
        if (!order.getStatus().equals(OrderStatus.COMPLETED)) {
            log.info("订单未完成，不能评价");
            return false;
        }
        
        // 检查订单是否属于当前用户
        if (!order.getUser().getId().equals(user.getId())) {
            log.info("订单不属于当前用户，不能评价");
            return false;
        }
        
        // 检查订单是否包含该商品
        boolean containsProduct = order.getOrderItems().stream()
                .anyMatch(item -> item.getProduct().getId().equals(productId));
        if (!containsProduct) {
            log.info("订单中不包含该商品，不能评价");
            return false;
        }
        
        // 检查是否已经评价过
        boolean hasReviewed = reviewRepository.existsByUserIdAndProductId(user.getId(), productId);
        if (hasReviewed) {
            log.info("用户已经评价过该商品");
            return false;
        }
        
        log.info("用户可以评价商品");
        return true;
    }

    @Override
    @Transactional
    public ReviewDTO createReview(Long productId, String username, CreateReviewRequest request) {
        log.info("开始创建评论: productId={}, username={}, orderId={}", 
                productId, username, request.getOrderId());
        
        // 检查用户是否存在
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("用户不存在: {}", username);
                    return new RuntimeException("用户不存在");
                });
                
        // 检查商品是否存在
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("商品不存在: {}", productId);
                    return new RuntimeException("商品不存在");
                });
                
        // 检查订单是否存在
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> {
                    log.error("订单不存在: {}", request.getOrderId());
                    return new RuntimeException("订单不存在");
                });
                
        // 检查评论权限
        if (!canUserReview(productId, username, request.getOrderId())) {
            log.error("用户无权评论: username={}, productId={}, orderId={}", 
                    username, productId, request.getOrderId());
            throw new RuntimeException("您没有权限评论该商品");
        }
        
        try {
            // 创建评论实体
            final Review reviewEntity = Review.builder()
                    .user(user)
                    .product(product)
                    .order(order)
                    .rating(request.getRating())
                    .content(request.getContent())
                    .anonymous(request.isAnonymous())
                    .visible(true)
                    .build();
                    
            // 先保存评论以获取ID
            final Review savedReview = reviewRepository.save(reviewEntity);
            log.debug("评论保存成功: {}", savedReview);
            
            // 如果有图片，创建并关联图片
            if (request.getImages() != null && !request.getImages().isEmpty()) {
                List<ReviewImage> reviewImages = request.getImages().stream()
                    .map(url -> {
                        ReviewImage image = new ReviewImage();
                        image.setImageUrl(url);
                        image.setReview(savedReview);
                        return image;
                    })
                    .collect(Collectors.toList());
                savedReview.setImages(reviewImages);
                final Review reviewWithImages = reviewRepository.save(savedReview);
                log.info("评论创建成功: reviewId={}", reviewWithImages.getId());
                return reviewMapper.toDTO(reviewWithImages);
            }
            
            log.info("评论创建成功: reviewId={}", savedReview.getId());
            return reviewMapper.toDTO(savedReview);
        } catch (Exception e) {
            log.error("保存评论失败: ", e);
            throw new RuntimeException("保存评论失败: " + e.getMessage());
        }
    }

    private ReviewDTO convertToDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .username(review.isAnonymous() ? "匿名用户" : review.getUser().getUsername())
                .userAvatar(review.isAnonymous() ? null : review.getUser().getAvatarUrl())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .orderId(review.getOrder().getId())
                .rating(review.getRating())
                .content(review.getContent())
                .images(review.getImages().stream()
                    .map(ReviewImage::getImageUrl)
                    .collect(Collectors.toList()))
                .anonymous(review.isAnonymous())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
} 