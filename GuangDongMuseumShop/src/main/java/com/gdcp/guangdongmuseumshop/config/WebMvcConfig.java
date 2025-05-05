package com.gdcp.guangdongmuseumshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.Path;
import java.nio.file.Paths;
import jakarta.annotation.PostConstruct;
import java.io.File;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Value("${file.upload-dir}")
    private String uploadPath;

    @PostConstruct
    public void init() {
        try {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                log.info("创建上传目录: {}", uploadPath);
                if (uploadDir.mkdirs()) {
                    log.info("成功创建上传目录");
                } else {
                    log.error("创建上传目录失败");
                    throw new RuntimeException("无法创建上传目录");
                }
            }
            log.info("上传目录的绝对路径: {}", uploadDir.getAbsolutePath());
        } catch (Exception e) {
            log.error("初始化上传目录时发生错误", e);
            throw new RuntimeException("无法初始化上传目录", e);
        }
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5174")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            File uploadDir = new File(uploadPath);
            String absolutePath = uploadDir.getAbsolutePath();
            String resourceLocation = "file:" + absolutePath + File.separator;
            
            log.info("配置文件上传路径映射: /uploads/** -> {}", resourceLocation);
            
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations(resourceLocation);
                    
            log.info("文件上传路径配置完成");
        } catch (Exception e) {
            log.error("配置资源处理器时发生错误", e);
            throw new RuntimeException("无法配置资源处理器", e);
        }
    }
} 