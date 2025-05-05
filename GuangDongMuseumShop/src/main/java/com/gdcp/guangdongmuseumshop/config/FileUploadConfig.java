package com.gdcp.guangdongmuseumshop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.util.unit.DataSize;
import java.io.File;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FileUploadConfig {

    @Value("${spring.servlet.multipart.location}")
    private String tempPath;

    @Bean
    public MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        return resolver;
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // 设置文件大小限制
        factory.setMaxFileSize(DataSize.ofMegabytes(10));
        factory.setMaxRequestSize(DataSize.ofMegabytes(10));
        
        // 确保临时目录存在
        try {
            File tempDir = new File(tempPath);
            if (!tempDir.exists()) {
                log.info("创建文件上传临时目录: {}", tempPath);
                if (tempDir.mkdirs()) {
                    log.info("成功创建临时目录");
                } else {
                    log.error("创建临时目录失败");
                    throw new RuntimeException("无法创建临时目录");
                }
            }
            
            // 设置临时文件目录
            factory.setLocation(tempDir.getAbsolutePath());
            log.info("设置文件上传临时目录: {}", tempDir.getAbsolutePath());
            
        } catch (Exception e) {
            log.error("配置文件上传时发生错误", e);
            throw new RuntimeException("无法配置文件上传", e);
        }
        
        return factory.createMultipartConfig();
    }
} 