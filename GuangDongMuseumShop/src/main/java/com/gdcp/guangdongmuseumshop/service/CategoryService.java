package com.gdcp.guangdongmuseumshop.service;

import com.gdcp.guangdongmuseumshop.dto.CategoryDTO;
import com.gdcp.guangdongmuseumshop.dto.CategoryTreeDTO;

import java.util.List;

public interface CategoryService {
    CategoryDTO create(CategoryDTO categoryDTO);
    CategoryDTO update(Long id, CategoryDTO categoryDTO);
    void delete(Long id);
    CategoryDTO getById(Long id);
    List<CategoryDTO> getAll();
    List<CategoryDTO> getByParentId(Long parentId);
    List<CategoryDTO> getAllCategories();
    List<CategoryTreeDTO> getCategoryTree();
} 