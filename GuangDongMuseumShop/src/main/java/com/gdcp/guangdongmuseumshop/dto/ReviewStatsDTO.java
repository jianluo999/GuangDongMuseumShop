package com.gdcp.guangdongmuseumshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatsDTO {
    private Long totalCount;
    private Double averageRating;
} 