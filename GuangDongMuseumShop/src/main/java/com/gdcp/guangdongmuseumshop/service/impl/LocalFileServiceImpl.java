package com.gdcp.guangdongmuseumshop.service.impl;

import com.gdcp.guangdongmuseumshop.exception.FileException;
import com.gdcp.guangdongmuseumshop.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Primary
@Service
public class LocalFileServiceImpl implements FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-url}")
    private String accessUrl;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif"
    );

    @Override
    public String uploadFile(MultipartFile file, String directory) {
        try {
            if (file.isEmpty()) {
                throw new FileException("Failed to store empty file");
            }

            if (!isValidImageFile(file)) {
                throw new FileException("Invalid image file type");
            }

            String fileName = generateFileName(file);
            Path targetLocation = getUploadPath(directory).resolve(fileName);
            
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            return getFileUrl(directory, fileName);
        } catch (IOException e) {
            throw new FileException("Failed to store file", e);
        }
    }

    @Override
    public List<String> uploadFiles(List<MultipartFile> files, String directory) {
        List<String> uploadedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            uploadedFiles.add(uploadFile(file, directory));
        }
        return uploadedFiles;
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return;
        }

        try {
            String relativePath = fileUrl.substring(accessUrl.length());
            Path filePath = Paths.get(uploadDir).resolve(relativePath.substring(1)).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new FileException("Failed to delete file", e);
        }
    }

    @Override
    public boolean isValidImageFile(MultipartFile file) {
        return ALLOWED_IMAGE_TYPES.contains(file.getContentType());
    }

    private Path getUploadPath(String directory) throws IOException {
        Path uploadPath = Paths.get(uploadDir, directory).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
        return uploadPath;
    }

    private String generateFileName(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFileName);
        return UUID.randomUUID().toString() + "." + extension;
    }

    private String getFileUrl(String directory, String fileName) {
        return accessUrl + "/" + directory + "/" + fileName;
    }
} 