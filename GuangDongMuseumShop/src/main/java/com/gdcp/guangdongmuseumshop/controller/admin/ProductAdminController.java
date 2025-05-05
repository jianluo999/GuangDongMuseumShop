package com.gdcp.guangdongmuseumshop.controller.admin;

import com.gdcp.guangdongmuseumshop.common.Result;
import com.gdcp.guangdongmuseumshop.dto.ProductDTO;
import com.gdcp.guangdongmuseumshop.dto.ProductSearchDTO;
import com.gdcp.guangdongmuseumshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ROLE_USER')")
@RequiredArgsConstructor
@Slf4j
public class ProductAdminController {

    private final ProductService productService;

    @GetMapping
    public Result<?> getProducts(ProductSearchDTO searchDTO, Pageable pageable) {
        log.info("开始查询商品列表");
        log.debug("查询参数: searchDTO={}, pageable={}", searchDTO, pageable);
        
        Page<ProductDTO> products = productService.getProducts(searchDTO, pageable);
        
        log.info("查询到商品数量: {}", products.getTotalElements());
        log.debug("商品列表: {}", products.getContent());
        
        return Result.success(products);
    }

    @PostMapping
    public Result<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        log.info("开始创建商品");
        log.debug("商品信息: {}", productDTO);
        
        ProductDTO created = productService.createProduct(productDTO);
        
        log.info("商品创建成功, id: {}", created.getId());
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<ProductDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductDTO productDTO) {
        log.info("开始更新商品, id: {}", id);
        log.debug("更新信息: {}", productDTO);
        
        productDTO.setId(id);
        ProductDTO updated = productService.updateProduct(productDTO);
        
        log.info("商品更新成功");
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result deleteProduct(@PathVariable Long id) {
        log.info("开始删除商品, id: {}", id);
        
        productService.deleteProduct(id);
        
        log.info("商品删除成功");
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/status")
    public Result<ProductDTO> updateProductStatus(@PathVariable Long id, @RequestBody Map<String, String> status) {
        String newStatus = status.get("status");
        log.info("开始更新商品状态, id: {}, status: {}", id, newStatus);
        
        ProductDTO updated = productService.updateProductStatus(id, newStatus);
        
        log.info("商品状态更新成功");
        return Result.success(updated);
    }

    @GetMapping("/{id}")
    public Result getProduct(@PathVariable Long id) {
        log.info("开始获取商品信息, id: {}", id);
        ProductDTO product = productService.getProductById(id);
        log.info("获取商品信息成功");
        return Result.success(product);
    }
} 