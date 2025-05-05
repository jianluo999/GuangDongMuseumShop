package com.gdcp.guangdongmuseumshop.service;

import com.gdcp.guangdongmuseumshop.dto.ProductSpecDTO;
import com.gdcp.guangdongmuseumshop.entity.SpecTemplate;
import java.util.List;

public interface SpecService {
    // 规格模板相关
    SpecTemplate getTemplateByCode(String code);
    List<SpecTemplate> getTemplatesByCategory(Long categoryId);
    List<SpecTemplate> getAllEnabledTemplates();
    
    // 商品规格相关
    List<ProductSpecDTO> getProductSpecs(Long productId);
    void saveProductSpecs(Long productId, List<ProductSpecDTO> specs);
    void deleteProductSpecs(Long productId);
    
    // 根据商品类型自动生成规格
    List<ProductSpecDTO> generateSpecsByTemplate(String templateCode);
} 