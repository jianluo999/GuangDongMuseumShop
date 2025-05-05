package com.gdcp.guangdongmuseumshop.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdcp.guangdongmuseumshop.dto.ProductSpecDTO;
import com.gdcp.guangdongmuseumshop.entity.Product;
import com.gdcp.guangdongmuseumshop.entity.ProductSpec;
import com.gdcp.guangdongmuseumshop.entity.SpecTemplate;
import com.gdcp.guangdongmuseumshop.exception.ResourceNotFoundException;
import com.gdcp.guangdongmuseumshop.repository.ProductRepository;
import com.gdcp.guangdongmuseumshop.repository.ProductSpecRepository;
import com.gdcp.guangdongmuseumshop.repository.SpecTemplateRepository;
import com.gdcp.guangdongmuseumshop.service.SpecService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpecServiceImpl implements SpecService {
    
    private final SpecTemplateRepository specTemplateRepository;
    private final ProductSpecRepository productSpecRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @Override
    public SpecTemplate getTemplateByCode(String code) {
        return specTemplateRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("规格模板不存在"));
    }

    @Override
    public List<SpecTemplate> getTemplatesByCategory(Long categoryId) {
        return specTemplateRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<SpecTemplate> getAllEnabledTemplates() {
        return specTemplateRepository.findByEnabledTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSpecDTO> getProductSpecs(Long productId) {
        List<ProductSpec> specs = productSpecRepository.findByProductIdOrderBySortOrder(productId);
        return specs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveProductSpecs(Long productId, List<ProductSpecDTO> specDTOs) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("商品不存在"));
        
        // 删除现有规格
        productSpecRepository.deleteByProductId(productId);
        
        // 保存新规格
        List<ProductSpec> specs = specDTOs.stream()
                .map(dto -> createProductSpec(product, dto))
                .collect(Collectors.toList());
        
        productSpecRepository.saveAll(specs);
    }

    @Override
    @Transactional
    public void deleteProductSpecs(Long productId) {
        productSpecRepository.deleteByProductId(productId);
    }

    @Override
    public List<ProductSpecDTO> generateSpecsByTemplate(String templateCode) {
        SpecTemplate template = getTemplateByCode(templateCode);
        try {
            Map<String, Object> specsMap = objectMapper.readValue(template.getSpecs(), 
                    new TypeReference<Map<String, Object>>() {});
            
            List<Map<String, Object>> specsList = (List<Map<String, Object>>) specsMap.get("specs");
            List<ProductSpecDTO> specs = new ArrayList<>();
            
            for (Map<String, Object> spec : specsList) {
                ProductSpecDTO specDTO = new ProductSpecDTO();
                specDTO.setName((String) spec.get("name"));
                
                List<Map<String, Object>> optionsMap = (List<Map<String, Object>>) spec.get("options");
                List<ProductSpecDTO.SpecOption> options = optionsMap.stream()
                        .map(opt -> {
                            ProductSpecDTO.SpecOption option = new ProductSpecDTO.SpecOption();
                            option.setValue((String) opt.get("value"));
                            option.setLabel((String) opt.get("label"));
                            option.setEnabled((Boolean) opt.get("enabled"));
                            option.setStock(100); // 默认库存
                            return option;
                        })
                        .collect(Collectors.toList());
                
                specDTO.setOptions(options);
                specs.add(specDTO);
            }
            
            return specs;
        } catch (JsonProcessingException e) {
            log.error("解析规格模板失败", e);
            throw new RuntimeException("解析规格模板失败");
        }
    }

    private ProductSpecDTO convertToDTO(ProductSpec spec) {
        ProductSpecDTO dto = new ProductSpecDTO();
        dto.setId(spec.getId());
        dto.setName(spec.getName());
        dto.setSortOrder(spec.getSortOrder());
        
        try {
            List<ProductSpecDTO.SpecOption> options = objectMapper.readValue(
                    spec.getOptions(),
                    new TypeReference<List<ProductSpecDTO.SpecOption>>() {}
            );
            dto.setOptions(options);
        } catch (JsonProcessingException e) {
            log.error("解析规格选项失败", e);
            throw new RuntimeException("解析规格选项失败");
        }
        
        return dto;
    }

    private ProductSpec createProductSpec(Product product, ProductSpecDTO dto) {
        ProductSpec spec = new ProductSpec();
        spec.setProduct(product);
        spec.setName(dto.getName());
        spec.setSortOrder(dto.getSortOrder());
        
        try {
            String options = objectMapper.writeValueAsString(dto.getOptions());
            spec.setOptions(options);
        } catch (JsonProcessingException e) {
            log.error("序列化规格选项失败", e);
            throw new RuntimeException("序列化规格选项失败");
        }
        
        return spec;
    }
} 