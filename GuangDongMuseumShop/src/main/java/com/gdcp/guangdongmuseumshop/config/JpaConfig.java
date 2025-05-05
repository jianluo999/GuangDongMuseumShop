package com.gdcp.guangdongmuseumshop.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA配置类
 * 启用JPA仓库和事务管理
 *
 * @since 1.0.0
 */
@Configuration
@EntityScan(basePackages = "com.gdcp.guangdongmuseumshop.entity")
@EnableJpaRepositories(basePackages = "com.gdcp.guangdongmuseumshop.repository")
public class JpaConfig {
}