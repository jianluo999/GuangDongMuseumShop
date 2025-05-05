package com.gdcp.guangdongmuseumshop.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "reviews")
public class Review {
    // ... 其他现有字段 ...
    
    @Column(name = "visible")
    private Boolean visible = true;
} 