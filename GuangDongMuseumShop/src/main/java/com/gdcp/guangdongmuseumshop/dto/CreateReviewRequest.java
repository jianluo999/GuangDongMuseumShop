package com.gdcp.guangdongmuseumshop.dto;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class CreateReviewRequest {
    
    private Long orderId;
    
    @Min(value = 1, message = "评分不能小于1")
    @Max(value = 5, message = "评分不能大于5")
    private Integer rating;
    
    @NotBlank(message = "评价内容不能为空")
    @Size(min = 5, max = 500, message = "评价内容长度必须在5-500字之间")
    private String content;
    
    private List<String> images;
    
    private boolean anonymous;
} 