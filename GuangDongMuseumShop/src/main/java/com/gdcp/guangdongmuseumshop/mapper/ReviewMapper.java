package com.gdcp.guangdongmuseumshop.mapper;

import com.gdcp.guangdongmuseumshop.dto.ReviewDTO;
import com.gdcp.guangdongmuseumshop.entity.Review;
import com.gdcp.guangdongmuseumshop.entity.ReviewImage;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "userAvatar", source = "user.avatarUrl")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "images", expression = "java(mapImagesToStrings(review.getImages()))")
    ReviewDTO toDTO(Review review);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "images", ignore = true)
    Review toEntity(ReviewDTO reviewDTO);

    @AfterMapping
    default void handleAnonymousUser(@MappingTarget ReviewDTO reviewDTO) {
        if (Boolean.TRUE.equals(reviewDTO.getAnonymous())) {
            reviewDTO.setUsername("匿名用户");
            reviewDTO.setUserAvatar(null);
        }
    }

    default List<String> mapImagesToStrings(List<ReviewImage> images) {
        if (images == null) return null;
        return images.stream()
            .map(ReviewImage::getImageUrl)
            .collect(Collectors.toList());
    }
} 