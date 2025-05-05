package com.gdcp.guangdongmuseumshop.service.impl;

import com.gdcp.guangdongmuseumshop.dto.CategoryDTO;
import com.gdcp.guangdongmuseumshop.dto.CategoryTreeDTO;
import com.gdcp.guangdongmuseumshop.entity.Category;
import com.gdcp.guangdongmuseumshop.repository.CategoryRepository;
import com.gdcp.guangdongmuseumshop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDTO create(CategoryDTO categoryDTO) {
        log.info("开始创建分类: {}", categoryDTO);
        
        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setParentId(categoryDTO.getParentId());
        category.setEnabled(true);
        
        // 设置默认排序值
        if (categoryDTO.getSort() == null) {
            // 获取同级分类中最大的排序值
            Integer maxSort = categoryRepository.findMaxSortByParentId(categoryDTO.getParentId());
            category.setSort(maxSort != null ? maxSort + 1 : 0);
        } else {
            category.setSort(categoryDTO.getSort());
        }
        
        // 设置层级
        if (categoryDTO.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDTO.getParentId())
                .orElseThrow(() -> new RuntimeException("父分类不存在"));
            category.setLevel(parent.getLevel() + 1);
        } else {
            category.setLevel(0);
        }
        
        category = categoryRepository.save(category);
        log.info("分类创建成功: {}", category);
        
        return convertToDTO(category);
    }

    @Override
    @Transactional
    public CategoryDTO update(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        updateCategoryFromDTO(category, categoryDTO);
        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        category.setEnabled(false);
        categoryRepository.save(category);
    }

    @Override
    public CategoryDTO getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        return convertToDTO(category);
    }

    @Override
    public List<CategoryDTO> getAll() {
        return categoryRepository.findByEnabledTrueOrderBySort().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDTO> getByParentId(Long parentId) {
        return categoryRepository.findByParentIdOrderBySort(parentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        log.info("执行分类查询");
        List<Category> categories = categoryRepository.findByEnabledTrue();
        log.info("查询到分类数量: {}", categories.size());
        log.debug("分类数据: {}", categories);
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryTreeDTO> getCategoryTree() {
        log.info("开始构建分类树");
        List<Category> allCategories = categoryRepository.findByEnabledTrue();
        List<CategoryTreeDTO> tree = buildTree(allCategories, null);
        log.info("分类树构建完成，根节点数量: {}", tree.size());
        return tree;
    }

    private List<CategoryTreeDTO> buildTree(List<Category> categories, Long parentId) {
        return categories.stream()
            .filter(cat -> {
                if (parentId == null) {
                    return cat.getParentId() == null;
                }
                return parentId.equals(cat.getParentId());
            })
            .map(cat -> {
                CategoryTreeDTO node = new CategoryTreeDTO();
                node.setId(cat.getId());
                node.setName(cat.getName());
                node.setDescription(cat.getDescription());
                node.setSort(cat.getSort());
                node.setEnabled(cat.getEnabled());
                node.setChildren(buildTree(categories, cat.getId()));
                return node;
            })
            .collect(Collectors.toList());
    }

    private void updateCategoryFromDTO(Category category, CategoryDTO dto) {
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setParentId(dto.getParentId());
        category.setLevel(dto.getLevel());
        category.setSort(dto.getSort());
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setParentId(category.getParentId());
        dto.setLevel(category.getLevel());
        dto.setSort(category.getSort());
        return dto;
    }
} 