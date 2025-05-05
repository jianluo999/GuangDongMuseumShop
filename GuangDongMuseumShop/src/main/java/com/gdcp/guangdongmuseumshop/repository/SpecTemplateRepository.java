package com.gdcp.guangdongmuseumshop.repository;

import com.gdcp.guangdongmuseumshop.entity.SpecTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SpecTemplateRepository extends JpaRepository<SpecTemplate, Long> {
    Optional<SpecTemplate> findByCode(String code);
    List<SpecTemplate> findByCategoryId(Long categoryId);
    List<SpecTemplate> findByEnabledTrue();
} 