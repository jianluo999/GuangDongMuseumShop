package com.gdcp.guangdongmuseumshop.controller.admin;

import com.gdcp.guangdongmuseumshop.common.Result;
import com.gdcp.guangdongmuseumshop.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ROLE_USER')")
@Slf4j
public class UploadController {

    @Value("${upload.path:/uploads}")
    private String uploadPath;

    private final FileService fileService;

    public UploadController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public Result<?> uploadFile(@RequestParam("file") MultipartFile file, 
                              @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("开始处理文件上传, Authorization: {}", authHeader);
        log.info("文件信息: name={}, size={}", file.getOriginalFilename(), file.getSize());

        try {
            String url = fileService.uploadFile(file, "products");
            log.info("文件上传成功, url: {}", url);
            return Result.success(url);
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            return Result.error(500, "文件上传失败: " + e.getMessage());
        }
    }
} 