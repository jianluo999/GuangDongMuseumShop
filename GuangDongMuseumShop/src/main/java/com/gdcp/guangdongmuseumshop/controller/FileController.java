package com.gdcp.guangdongmuseumshop.controller;

import com.gdcp.guangdongmuseumshop.common.Result;
import com.gdcp.guangdongmuseumshop.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/files")
@PreAuthorize("hasRole('ADMIN')")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            log.info("开始上传文件: {}", file.getOriginalFilename());
            String fileUrl = fileService.uploadFile(file, "files");
            log.info("文件上传成功: {}", fileUrl);
            return Result.success(fileUrl);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error(500, "文件上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (!fileService.isValidImageFile(file)) {
                return Result.error(400, "只支持上传图片文件");
            }
            log.info("开始上传图片: {}", file.getOriginalFilename());
            String fileUrl = fileService.uploadFile(file, "images");
            log.info("图片上传成功: {}", fileUrl);
            return Result.success(fileUrl);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return Result.error(500, "图片上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload/images")
    public Result<List<String>> uploadImages(@RequestParam("files") List<MultipartFile> files) {
        try {
            log.info("开始批量上传图片, 数量: {}", files.size());
            List<String> fileUrls = fileService.uploadFiles(files, "images");
            log.info("批量上传图片成功, 数量: {}", fileUrls.size());
            return Result.success(fileUrls);
        } catch (Exception e) {
            log.error("批量上传图片失败", e);
            return Result.error(500, "批量上传图片失败: " + e.getMessage());
        }
    }

    @DeleteMapping
    public Result<String> deleteFile(@RequestParam("url") String fileUrl) {
        try {
            log.info("开始删除文件: {}", fileUrl);
            fileService.deleteFile(fileUrl);
            log.info("文件删除成功");
            return Result.success("文件删除成功");
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return Result.error(500, "文件删除失败: " + e.getMessage());
        }
    }
} 