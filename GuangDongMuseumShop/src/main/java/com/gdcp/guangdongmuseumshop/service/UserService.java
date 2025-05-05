package com.gdcp.guangdongmuseumshop.service;

import com.gdcp.guangdongmuseumshop.dto.PasswordUpdateDTO;
import com.gdcp.guangdongmuseumshop.dto.RegisterDTO;
import com.gdcp.guangdongmuseumshop.dto.UserDTO;
import com.gdcp.guangdongmuseumshop.dto.UserProfileDTO;
import com.gdcp.guangdongmuseumshop.dto.UserSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface UserService {
    UserProfileDTO getUserProfile(Long userId);
    UserProfileDTO getUserProfile(String username);
    UserProfileDTO updateProfile(Long userId, UserProfileDTO profileDTO);
    void updatePassword(Long userId, PasswordUpdateDTO passwordDTO);
    String updateAvatar(Long userId, MultipartFile file);
    
    // 新增用户管理方法
    Page<UserProfileDTO> searchUsers(UserSearchDTO searchDTO, Pageable pageable);
    void enableUser(Long userId);
    void disableUser(Long userId);
    void deleteUser(Long userId);
    UserDTO register(RegisterDTO registerDTO);
    UserProfileDTO createUser(UserProfileDTO userDTO);
    Map<String, String> resetPassword(Long userId);
    UserProfileDTO updateUser(Long userId, UserProfileDTO userDTO);
    List<UserProfileDTO> getAllUsers();
} 