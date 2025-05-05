package com.gdcp.guangdongmuseumshop.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    @Column(name = "parent_id")
    private Long parentId;

    private Integer level;

    private Integer sort;

    @Column(nullable = false)
    private Boolean enabled = true;
} 