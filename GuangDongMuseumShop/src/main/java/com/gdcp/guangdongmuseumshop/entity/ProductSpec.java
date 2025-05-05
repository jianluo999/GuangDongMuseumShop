package com.gdcp.guangdongmuseumshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.Type;
import java.util.List;

@Data
@Entity
@Table(name = "product_specs")
public class ProductSpec {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @ToString.Exclude
    private Product product;

    @Column(name = "spec_name", nullable = false)
    private String name;

    @Column(name = "spec_options", columnDefinition = "json")
    private String options;

    @Column(name = "sort_order")
    private Integer sortOrder;
} 