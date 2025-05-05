package com.gdcp.guangdongmuseumshop.controller;

import com.gdcp.guangdongmuseumshop.common.Result;
import com.gdcp.guangdongmuseumshop.dto.ProductSpecDTO;
import com.gdcp.guangdongmuseumshop.entity.SpecTemplate;
import com.gdcp.guangdongmuseumshop.service.SpecService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class SpecController {
    
    private final SpecService specService;
    
    @GetMapping("/specs/templates")
    public Result<?> getAllTemplates() {
        List<SpecTemplate> templates = specService.getAllEnabledTemplates();
        return Result.success(templates);
    }
    
    @GetMapping("/specs/templates/{categoryId}")
    public Result<?> getTemplatesByCategory(@PathVariable Long categoryId) {
        List<SpecTemplate> templates = specService.getTemplatesByCategory(categoryId);
        return Result.success(templates);
    }
    
    @GetMapping("/specs/templates/code/{code}")
    public Result<?> getTemplateByCode(@PathVariable String code) {
        SpecTemplate template = specService.getTemplateByCode(code);
        return Result.success(template);
    }
    
    @GetMapping("/products/{productId}/specs")
    public Result<?> getProductSpecs(@PathVariable Long productId) {
        List<ProductSpecDTO> specs = specService.getProductSpecs(productId);
        return Result.success(specs);
    }
    
    @PostMapping("/products/{productId}/specs")
    public Result<?> saveProductSpecs(
            @PathVariable Long productId,
            @RequestBody List<ProductSpecDTO> specs) {
        specService.saveProductSpecs(productId, specs);
        return Result.success(true);
    }
    
    @DeleteMapping("/products/{productId}/specs")
    public Result<?> deleteProductSpecs(@PathVariable Long productId) {
        specService.deleteProductSpecs(productId);
        return Result.success(true);
    }
    
    @GetMapping("/specs/generate")
    public Result<?> generateSpecs(@RequestParam String templateCode) {
        List<ProductSpecDTO> specs = specService.generateSpecsByTemplate(templateCode);
        return Result.success(specs);
    }
} 