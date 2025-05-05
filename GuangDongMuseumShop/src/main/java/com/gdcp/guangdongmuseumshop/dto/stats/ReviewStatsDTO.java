package com.gdcp.guangdongmuseumshop.dto.stats;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatsDTO implements Serializable {
    private Long productId;
    private String productName;
    private Double averageRating = 0.0;
    private Long totalCount = 0L;
    private Long fiveStarCount = 0L;
    private Long fourStarCount = 0L;
    private Long threeStarCount = 0L;
    private Long twoStarCount = 0L;
    private Long oneStarCount = 0L;
    private Double positiveRate = 0.0;

    public ReviewStatsDTO(Long productId, String productName, Double averageRating, 
            Long totalCount, Long fiveStarCount, Long fourStarCount, 
            Long threeStarCount, Long twoStarCount, Long oneStarCount) {
        this.productId = productId;
        this.productName = productName;
        this.averageRating = averageRating != null ? averageRating : 0.0;
        this.totalCount = totalCount != null ? totalCount : 0L;
        this.fiveStarCount = fiveStarCount != null ? fiveStarCount : 0L;
        this.fourStarCount = fourStarCount != null ? fourStarCount : 0L;
        this.threeStarCount = threeStarCount != null ? threeStarCount : 0L;
        this.twoStarCount = twoStarCount != null ? twoStarCount : 0L;
        this.oneStarCount = oneStarCount != null ? oneStarCount : 0L;
        
        // 计算好评率
        if (this.totalCount > 0) {
            this.positiveRate = (double) (this.fiveStarCount + this.fourStarCount) / this.totalCount * 100;
        } else {
            this.positiveRate = 0.0;
        }
    }

    // 添加一个静态方法来创建空的统计对象
    public static ReviewStatsDTO createEmpty(Long productId, String productName) {
        return new ReviewStatsDTO(productId, productName, 0.0, 0L, 0L, 0L, 0L, 0L, 0L);
    }
} 