package com.gdcp.guangdongmuseumshop.repository;

import com.gdcp.guangdongmuseumshop.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIdOrderBySort(Long parentId);
    List<Category> findByEnabledTrueOrderBySort();
    List<Category> findByEnabledTrue();

    @Query("SELECT MAX(c.sort) FROM Category c WHERE c.parentId = :parentId")
    Integer findMaxSortByParentId(@Param("parentId") Long parentId);
} 