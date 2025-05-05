package com.gdcp.guangdongmuseumshop.service.impl;

import com.gdcp.guangdongmuseumshop.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {
    
    @Value("${upload.path:/uploads}")
    private String uploadPath;

    @Override
    public String uploadFile(MultipartFile file, String directory) throws Exception {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + extension;
        
        File uploadDir = new File(uploadPath + "/" + directory);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        File destFile = new File(uploadDir, newFilename);
        file.transferTo(destFile);
        
        return "/uploads/" + directory + "/" + newFilename;
    }

    @Override
    public List<String> uploadFiles(List<MultipartFile> files, String directory) {
        return files.stream()
            .map(file -> {
                try {
                    return uploadFile(file, directory);
                } catch (Exception e) {
                    return null;
                }
            })
            .filter(url -> url != null)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl != null && fileUrl.startsWith("/uploads/")) {
            File file = new File(uploadPath, fileUrl.substring(9));
            if (file.exists()) {
                file.delete();
            }
        }
    }

    @Override
    public boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
} 