package com.gdcp.guangdongmuseumshop.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import java.util.List;

@Data
public class ReviewRequest {
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer rating;

    @NotBlank(message = "评价内容不能为空")
    @Length(min = 5, max = 500, message = "评价内容长度需在5-500字之间")
    private String content;
    
    private List<String> images;
} 