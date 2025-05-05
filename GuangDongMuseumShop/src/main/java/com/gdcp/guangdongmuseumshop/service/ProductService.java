package com.gdcp.guangdongmuseumshop.service;

import com.gdcp.guangdongmuseumshop.dto.ProductDTO;
import com.gdcp.guangdongmuseumshop.dto.ProductDetailDTO;
import com.gdcp.guangdongmuseumshop.dto.ProductSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ProductService {
    ProductDTO create(ProductDTO productDTO);
    ProductDTO update(Long id, ProductDTO productDTO);
    void delete(Long id);
    ProductDTO getById(Long id);
    Page<ProductDTO> getByCategory(Long categoryId, Pageable pageable);
    List<ProductDetailDTO> getAllProductDetails();
    Page<ProductDTO> getProducts(ProductSearchDTO searchDTO, Pageable pageable);
    Page<ProductDTO> searchProducts(String keyword, Long categoryId, Pageable pageable);
    ProductDTO updateProductStatus(Long id, String status);
    ProductDTO createProduct(ProductDTO productDTO);
    ProductDTO updateProduct(ProductDTO productDTO);
    void deleteProduct(Long id);
    ProductDTO getProductById(Long id);
    List<String> getHotSearches();
    Page<ProductDTO> getFeaturedProducts(String priceRange, String sort, Pageable pageable);
    Page<ProductDTO> getProductsByCategory(Long categoryId, String priceRange, String sort, Pageable pageable);
    List<ProductDTO> getRecommendedProducts();
    Page<ProductDTO> getNewProducts(String priceRange, String sort, Pageable pageable);
} 