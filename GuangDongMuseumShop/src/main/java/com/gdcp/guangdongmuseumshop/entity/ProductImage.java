package com.gdcp.guangdongmuseumshop.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
@Data
@Entity
@Table(name = "product_images")
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @ToString.Exclude
    private Product product;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    private Integer sort = 0;
} 