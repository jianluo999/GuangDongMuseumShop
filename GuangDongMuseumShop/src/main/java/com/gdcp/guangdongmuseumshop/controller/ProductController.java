package com.gdcp.guangdongmuseumshop.controller;

import com.gdcp.guangdongmuseumshop.dto.ProductDTO;
import com.gdcp.guangdongmuseumshop.service.ProductService;
import com.gdcp.guangdongmuseumshop.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping("/search")
    public Result<?> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            Pageable pageable) {
        Page<ProductDTO> products = productService.searchProducts(keyword, categoryId, pageable);
        return Result.success(products);
    }

    @GetMapping("/hot-searches")
    public Result<List<String>> getHotSearches() {
        log.info("获取热门搜索关键词");
        try {
            List<String> hotSearches = productService.getHotSearches();
            
            // 如果没有搜索历史，返回默认的热门关键词
            if (hotSearches.isEmpty()) {
                hotSearches = Arrays.asList(
                    "文创礼品",
                    "非遗文创",
                    "茶器茶具",
                    "丝巾织品",
                    "文房四宝"
                );
            }
            
            return Result.success(hotSearches);
        } catch (Exception e) {
            log.error("获取热门搜索关键词失败: ", e);
            return Result.error(500, "获取热门搜索失败");
        }
    }

    @GetMapping("/featured")
    public Result<?> getFeaturedProducts(
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) String sort,
            Pageable pageable) {
        log.info("获取精选文创商品列表, priceRange={}, sort={}, pageable={}", priceRange, sort, pageable);
        try {
            Page<ProductDTO> products = productService.getFeaturedProducts(priceRange, sort, pageable);
            return Result.success(products);
        } catch (Exception e) {
            log.error("获取精选文创商品列表失败", e);
            return Result.error(500, "获取商品列表失败");
        }
    }

    @GetMapping("/category/{categoryId}")
    public Result<?> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) String sort,
            Pageable pageable) {
        log.info("获取分类商品列表, categoryId={}, priceRange={}, sort={}, pageable={}", 
                categoryId, priceRange, sort, pageable);
        try {
            Page<ProductDTO> products = productService.getProductsByCategory(categoryId, priceRange, sort, pageable);
            return Result.success(products);
        } catch (Exception e) {
            log.error("获取分类商品列表失败", e);
            return Result.error(500, "获取商品列表失败");
        }
    }

    @GetMapping("/{id}")
    public Result<?> getProductDetail(@PathVariable Long id) {
        log.info("获取商品详情, id: {}", id);
        try {
            ProductDTO product = productService.getProductById(id);
            if (product == null) {
                return Result.error(404, "商品不存在或已下架");
            }
            return Result.success(product);
        } catch (Exception e) {
            log.error("获取商品详情失败: ", e);
            return Result.error(500, "获取商品详情失败");
        }
    }

    @GetMapping("/recommended")
    @PreAuthorize("permitAll()")
    public Result<?> getRecommendedProducts() {
        log.info("获取推荐商品列表");
        try {
            List<ProductDTO> products = productService.getRecommendedProducts();
            return Result.success(products);
        } catch (Exception e) {
            log.error("获取推荐商品列表失败", e);
            return Result.error(500, "获取推荐商品列表失败");
        }
    }

    @GetMapping("/new")
    public Result<?> getNewProducts(
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) String sort,
            Pageable pageable) {
        log.info("获取新品列表, priceRange={}, sort={}, pageable={}", priceRange, sort, pageable);
        try {
            Page<ProductDTO> products = productService.getNewProducts(priceRange, sort, pageable);
            return Result.success(products);
        } catch (Exception e) {
            log.error("获取新品列表失败", e);
            return Result.error(500, "获取新品列表失败");
        }
    }
} 