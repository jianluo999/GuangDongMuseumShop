package com.gdcp.guangdongmuseumshop.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "search_history")
public class SearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String keyword;

    @Column(name = "search_time", nullable = false)
    private LocalDateTime searchTime;

    @Column(name = "search_count")
    private Integer searchCount = 1;
} 