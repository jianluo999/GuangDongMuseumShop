package com.gdcp.guangdongmuseumshop.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface FileService {
    String uploadFile(MultipartFile file, String directory) throws Exception;
    List<String> uploadFiles(List<MultipartFile> files, String directory);
    void deleteFile(String fileUrl);
    boolean isValidImageFile(MultipartFile file);
} 