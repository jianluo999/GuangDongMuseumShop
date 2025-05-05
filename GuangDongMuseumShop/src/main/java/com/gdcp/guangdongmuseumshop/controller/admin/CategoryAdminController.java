package com.gdcp.guangdongmuseumshop.controller.admin;

import com.gdcp.guangdongmuseumshop.common.Result;
import com.gdcp.guangdongmuseumshop.dto.CategoryDTO;
import com.gdcp.guangdongmuseumshop.dto.CategoryTreeDTO;
import com.gdcp.guangdongmuseumshop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ROLE_USER', 'ROLE_ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class CategoryAdminController {
    
    private final CategoryService categoryService;

    @GetMapping
    public Result<Page<CategoryDTO>> getCategories(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        
        PageRequest pageRequest = PageRequest.of(page, size);
        List<CategoryDTO> categories = categoryService.getAllCategories();
        
        // 手动分页
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), categories.size());
        Page<CategoryDTO> pageResult = new PageImpl<>(
            categories.subList(start, end), 
            pageRequest, 
            categories.size()
        );
        
        return Result.success(pageResult);
    }

    @GetMapping("/all")
    public Result<?> getAllCategories() {
        return Result.success(categoryService.getAllCategories());
    }

    @GetMapping("/tree")
    public Result<?> getCategoryTree() {
        log.info("开始获取分类树");
        List<CategoryTreeDTO> tree = categoryService.getCategoryTree();
        log.info("获取分类树成功, 共 {} 条数据", tree.size());
        return Result.success(tree);
    }

    @PostMapping
    public Result<?> createCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("开始创建分类");
        log.debug("分类信息: {}", categoryDTO);
        try {
            CategoryDTO created = categoryService.create(categoryDTO);
            log.info("分类创建成功, id: {}", created.getId());
            return Result.success(created);
        } catch (Exception e) {
            log.error("创建分类失败:", e);
            return Result.error(500, "创建分类失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<?> updateCategory(@PathVariable Long id, @RequestBody CategoryDTO categoryDTO) {
        log.info("开始更新分类, id: {}", id);
        log.debug("更新信息: {}", categoryDTO);
        try {
            CategoryDTO updated = categoryService.update(id, categoryDTO);
            log.info("分类更新成功");
            return Result.success(updated);
        } catch (Exception e) {
            log.error("更新分类失败:", e);
            return Result.error(500, "更新分类失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        log.info("开始删除分类, id: {}", id);
        try {
            categoryService.delete(id);
            log.info("分类删除成功");
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除分类失败:", e);
            return Result.error(500, "删除分类失败：" + e.getMessage());
        }
    }
} 