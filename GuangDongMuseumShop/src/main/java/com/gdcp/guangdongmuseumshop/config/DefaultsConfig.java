package com.gdcp.guangdongmuseumshop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.defaults")
public class DefaultsConfig {
    private String avatar;
    private String productImage;
    private String categoryImage;
} 