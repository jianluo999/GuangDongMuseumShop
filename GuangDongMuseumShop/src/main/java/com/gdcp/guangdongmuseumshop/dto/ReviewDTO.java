package com.gdcp.guangdongmuseumshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private Long userId;
    private String username;
    private String userAvatar;
    private Long productId;
    private String productName;
    private Long orderId;
    private Integer rating;
    private String content;
    private List<String> images;
    private Boolean anonymous;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 