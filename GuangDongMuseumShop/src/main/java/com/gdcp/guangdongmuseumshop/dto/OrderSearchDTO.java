package com.gdcp.guangdongmuseumshop.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSearchDTO {
    private String orderNo;
    private String username;
    private String status;
    private String startTime;
    private String endTime;
} 