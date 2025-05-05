package com.gdcp.guangdongmuseumshop.mapper;

import com.gdcp.guangdongmuseumshop.dto.ProductDTO;
import com.gdcp.guangdongmuseumshop.entity.Product;
import com.gdcp.guangdongmuseumshop.entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper
public interface ProductMapper {
    @Mapping(target = "images", expression = "java(mapImages(product.getImages()))")
    ProductDTO toDTO(Product product);
    
    @Mapping(target = "images", ignore = true)
    Product toEntity(ProductDTO productDTO);

    default List<String> mapImages(List<ProductImage> images) {
        if (images == null) return null;
        return images.stream()
            .map(ProductImage::getUrl)
            .toList();
    }
} 