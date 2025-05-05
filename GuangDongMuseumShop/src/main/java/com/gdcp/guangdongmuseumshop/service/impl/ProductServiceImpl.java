package com.gdcp.guangdongmuseumshop.service.impl;

import com.gdcp.guangdongmuseumshop.dto.ProductDTO;
import com.gdcp.guangdongmuseumshop.dto.ProductDetailDTO;
import com.gdcp.guangdongmuseumshop.entity.Product;
import com.gdcp.guangdongmuseumshop.entity.ProductImage;
import com.gdcp.guangdongmuseumshop.entity.Review;
import com.gdcp.guangdongmuseumshop.repository.CategoryRepository;
import com.gdcp.guangdongmuseumshop.repository.ProductImageRepository;
import com.gdcp.guangdongmuseumshop.repository.ProductRepository;
import com.gdcp.guangdongmuseumshop.service.ProductService;
import com.gdcp.guangdongmuseumshop.config.DefaultsConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import com.gdcp.guangdongmuseumshop.dto.ProductSearchDTO;
import com.gdcp.guangdongmuseumshop.exception.ResourceNotFoundException;
import com.gdcp.guangdongmuseumshop.repository.SearchHistoryRepository;
import com.gdcp.guangdongmuseumshop.entity.SearchHistory;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import jakarta.persistence.criteria.JoinType;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final DefaultsConfig defaultsConfig;
    private final SearchHistoryRepository searchHistoryRepository;

    @Override
    @Transactional
    public ProductDTO create(ProductDTO productDTO) {
        Product product = new Product();
        updateProductFromDTO(product, productDTO);
        product = productRepository.save(product);
        saveProductImages(product, productDTO);
        return convertToDTO(product);
    }

    @Override
    @Transactional
    public ProductDTO update(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        updateProductFromDTO(product, productDTO);
        product = productRepository.save(product);
        saveProductImages(product, productDTO);
        return convertToDTO(product);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        product.setEnabled(false);
        productRepository.save(product);
    }

    @Override
    public ProductDTO getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        return convertToDTO(product);
    }

    @Override
    public Page<ProductDTO> getByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndEnabledTrue(categoryId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public List<ProductDetailDTO> getAllProductDetails() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProducts(ProductSearchDTO searchDTO, Pageable pageable) {
        log.info("执行商品查询");
        log.debug("查询条件: {}", searchDTO);
        
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(searchDTO.getName())) {
                predicates.add(cb.like(root.get("name"), "%" + searchDTO.getName() + "%"));
            }
            if (searchDTO.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), searchDTO.getCategoryId()));
            }
            if (StringUtils.hasText(searchDTO.getStatus())) {
                predicates.add(cb.equal(root.get("enabled"), "ON_SALE".equals(searchDTO.getStatus())));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<Product> products = productRepository.findAll(spec, pageable);

        log.info("查询到商品数量: {}", products.getTotalElements());
        log.debug("商品数据: {}", products.getContent());

        return products.map(this::convertToDTO);
    }

    @Override
    public Page<ProductDTO> searchProducts(String keyword, Long categoryId, Pageable pageable) {
        log.info("========== 开始搜索商品 ==========");
        log.info("搜索参数 - 关键词: {}, 分类ID: {}, 分页: {}", keyword, categoryId, pageable);
        
        try {
            // 如果有关键词，保存搜索历史
            if (StringUtils.hasText(keyword)) {
                saveSearchHistory(keyword);
            }

            // 使用 Specification 构建动态查询条件
            Specification<Product> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                // 只搜索已启用的商品
                predicates.add(cb.isTrue(root.get("enabled")));
                
                // 添加关键词搜索条件
                if (StringUtils.hasText(keyword)) {
                    String likePattern = "%" + keyword.toLowerCase() + "%";
                    predicates.add(
                        cb.or(
                            cb.like(cb.lower(root.get("name")), likePattern),
                            cb.like(cb.lower(root.get("description")), likePattern)
                        )
                    );
                }
                
                // 添加分类条件
                if (categoryId != null) {
                    predicates.add(cb.equal(root.get("category").get("id"), categoryId));
                }
                
                // 获取根查询并添加 fetch join
                if (query.getResultType().equals(Product.class)) {
                    root.fetch("category", JoinType.LEFT);
                    root.fetch("images", JoinType.LEFT);
                    query.distinct(true);
                }
                
                return cb.and(predicates.toArray(new Predicate[0]));
            };

            Page<Product> products = productRepository.findAll(spec, pageable);
            
            log.info("查询结果 - 总数: {}, 当前页数据量: {}", 
                products.getTotalElements(), products.getContent().size());
            
            // 转换为DTO并处理图片URL
            Page<ProductDTO> dtoPage = products.map(product -> {
                log.info("===== 开始处理商品 ID: {} =====", product.getId());
                log.info("商品名称: {}", product.getName());
                log.info("原始主图: {}", product.getMainImage());
                log.info("原始图片列表: {}", product.getImages().stream()
                    .map(ProductImage::getImageUrl)
                    .collect(Collectors.toList()));
                
                ProductDTO dto = convertToDTO(product);
                
                log.info("转换后的DTO - 主图: {}", dto.getMainImage());
                log.info("转换后的DTO - 图片列表: {}", dto.getImages());
                log.info("===== 商品处理完成 =====");
                
                return dto;
            });
            
            return dtoPage;
        } catch (Exception e) {
            log.error("搜索商品时发生错误: ", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ProductDTO updateProductStatus(Long id, String status) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("商品不存在"));
        product.setEnabled("ON_SALE".equals(status));
        product = productRepository.save(product);
        return convertToDTO(product);
    }

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        return create(productDTO);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(ProductDTO productDTO) {
        return update(productDTO.getId(), productDTO);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("开始删除商品: {}", id);
        productImageRepository.deleteByProductId(id);
        productRepository.deleteById(id);
        log.info("商品删除完成");
    }

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("商品不存在"));
        return convertToDTO(product);
    }

    @Override
    public List<String> getHotSearches() {
        return searchHistoryRepository.findTopSearches(10);
    }

    @Override
    public Page<ProductDTO> getFeaturedProducts(String priceRange, String sort, Pageable pageable) {
        log.info("获取精选文创商品, priceRange={}, sort={}, pageable={}", priceRange, sort, pageable);
        
        return productRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 添加价格范围条件
            if (StringUtils.hasText(priceRange)) {
                String[] range = priceRange.split("-");
                if (range.length == 2) {
                    try {
                        BigDecimal min = new BigDecimal(range[0]);
                        BigDecimal max = new BigDecimal(range[1]);
                        predicates.add(cb.between(root.get("price"), min, max));
                    } catch (NumberFormatException e) {
                        log.error("价格范围格式错误: {}", priceRange);
                    }
                }
            }
            
            // 获取根查询并添加 fetch join
            if (query.getResultType().equals(Product.class)) {
                root.fetch("images", JoinType.LEFT);  // 使用 images 而不是 productImages
                query.distinct(true);  // 避免笛卡尔积导致的重复结果
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable).map(this::convertToDTO);
    }

    @Override
    public Page<ProductDTO> getProductsByCategory(Long categoryId, String priceRange, String sort, Pageable pageable) {
        log.info("获取分类商品, categoryId={}, priceRange={}, sort={}, pageable={}", 
                categoryId, priceRange, sort, pageable);
        
        return productRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 添加分类条件
            predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            
            // 添加价格区间条件
            if (StringUtils.hasText(priceRange)) {
                String[] range = priceRange.split("-");
                if (range.length == 2) {
                    predicates.add(cb.between(root.get("price"), 
                        new BigDecimal(range[0]), new BigDecimal(range[1])));
                } else if (priceRange.endsWith("+")) {
                    predicates.add(cb.greaterThanOrEqualTo(
                        root.get("price"), new BigDecimal(priceRange.replace("+", ""))));
                }
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable).map(this::convertToDTO);
    }

    @Override
    public List<ProductDTO> getRecommendedProducts() {
        log.info("获取推荐商品列表");
        
        // 获取随机的6个上架商品
        List<Product> products = productRepository.findRandomEnabledProducts(6);
        
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductDTO> getNewProducts(String priceRange, String sort, Pageable pageable) {
        log.info("获取新品列表, priceRange={}, sort={}, pageable={}", priceRange, sort, pageable);
        
        return productRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 只获取上架的商品
            predicates.add(cb.isTrue(root.get("enabled")));
            
            // 添加价格区间条件
            if (StringUtils.hasText(priceRange)) {
                String[] range = priceRange.split("-");
                if (range.length == 2) {
                    predicates.add(cb.between(root.get("price"), 
                        new BigDecimal(range[0]), new BigDecimal(range[1])));
                } else if (priceRange.endsWith("+")) {
                    predicates.add(cb.greaterThanOrEqualTo(
                        root.get("price"), new BigDecimal(priceRange.replace("+", ""))));
                }
            }
            
            // 获取最近30天内的商品
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), thirtyDaysAgo));
            
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable).map(this::convertToDTO);
    }

    private void updateProductFromDTO(Product product, ProductDTO dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setMainImage(dto.getMainImage());
        product.setCulturalBackground(dto.getCulturalBackground());
        product.setCategory(categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("分类不存在")));
    }

    private void saveProductImages(Product product, ProductDTO dto) {
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            productImageRepository.deleteAll(
                    productImageRepository.findByProductIdOrderBySort(product.getId())
            );
            
            int sort = 0;
            for (String imageUrl : dto.getImages()) {
                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setImageUrl(imageUrl);
                image.setSort(sort++);
                productImageRepository.save(image);
            }
        }
    }

    private ProductDTO convertToDTO(Product product) {
        log.info("开始转换商品 ID: {} 为 DTO", product.getId());
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        
        // 处理商品图片列表
        List<String> imageUrls = new ArrayList<>();
        
        // 1. 处理主图
        String mainImage = product.getMainImage();
        log.info("处理主图 - 原始值: {}", mainImage);
        
        if (mainImage != null && !mainImage.trim().isEmpty() && !"(Null)".equals(mainImage)) {
            String processedMainImage = !mainImage.startsWith("http") && !mainImage.startsWith("/") 
                ? "/" + mainImage 
                : mainImage;
            log.info("处理后的主图URL: {}", processedMainImage);
            imageUrls.add(processedMainImage);
            dto.setMainImage(processedMainImage);
        } else {
            log.info("主图无效或为(Null)，设置为null");
            dto.setMainImage(null);
        }
        
        // 2. 添加其他图片
        var images = productImageRepository.findByProductIdOrderBySort(product.getId());
        log.info("获取到的其他图片数量: {}", images.size());
        
        images.stream()
            .map(ProductImage::getImageUrl)
            .filter(url -> {
                boolean isValid = url != null && !url.trim().isEmpty() && !"(Null)".equals(url);
                if (!isValid) {
                    log.info("过滤掉无效的图片URL: {}", url);
                }
                return isValid;
            })
            .map(url -> {
                String processedUrl = !url.startsWith("http") && !url.startsWith("/") ? "/" + url : url;
                log.info("处理图片URL: {} -> {}", url, processedUrl);
                return processedUrl;
            })
            .forEach(imageUrls::add);
        
        // 3. 如果没有任何图片，使用默认图片
        if (imageUrls.isEmpty()) {
            log.info("没有有效图片，使用默认图片");
            String defaultImage = defaultsConfig.getProductImage();
            imageUrls.add(defaultImage);
            dto.setMainImage(defaultImage);
        } else if (dto.getMainImage() == null && !imageUrls.isEmpty()) {
            log.info("使用第一张图片作为主图: {}", imageUrls.get(0));
            dto.setMainImage(imageUrls.get(0));
        }
        
        log.info("最终图片列表: {}", imageUrls);
        dto.setImages(imageUrls);
        dto.setCulturalBackground(product.getCulturalBackground());
        dto.setCategoryId(product.getCategory().getId());
        dto.setStatus(product.getEnabled() ? "ON_SALE" : "OFF_SALE");
        
        log.info("DTO转换完成: {}", dto);
        return dto;
    }

    private ProductDetailDTO convertToDetailDTO(Product product) {
        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setEnabled(product.getEnabled());
        dto.setCategoryName(product.getCategory().getName());
        dto.setCategoryId(product.getCategory().getId());
        dto.setImageUrls(product.getImages().stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList()));
        
        // 计算平均评分和评论数
        List<Review> reviews = product.getReviews();
        dto.setReviewCount((long) reviews.size());
        dto.setAverageRating(reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0));
        
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }

    private void saveSearchHistory(String keyword) {
        try {
            SearchHistory searchHistory = searchHistoryRepository.findByKeyword(keyword)
                .orElse(new SearchHistory());
                
            if (searchHistory.getId() == null) {
                searchHistory.setKeyword(keyword);
                searchHistory.setSearchCount(1);
            } else {
                searchHistory.setSearchCount(searchHistory.getSearchCount() + 1);
            }
            
            searchHistory.setSearchTime(LocalDateTime.now());
            searchHistoryRepository.save(searchHistory);
        } catch (Exception e) {
            log.error("保存搜索历史失败: ", e);
        }
    }
} 