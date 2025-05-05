package com.gdcp.guangdongmuseumshop.service.impl;

import com.gdcp.guangdongmuseumshop.dto.PasswordUpdateDTO;
import com.gdcp.guangdongmuseumshop.dto.UserProfileDTO;
import com.gdcp.guangdongmuseumshop.dto.UserSearchDTO;
import com.gdcp.guangdongmuseumshop.dto.UserDTO;
import com.gdcp.guangdongmuseumshop.dto.RegisterDTO;
import com.gdcp.guangdongmuseumshop.entity.Role;
import com.gdcp.guangdongmuseumshop.exception.OrderException;
import com.gdcp.guangdongmuseumshop.exception.UserException;
import com.gdcp.guangdongmuseumshop.entity.User;
import com.gdcp.guangdongmuseumshop.repository.UserRepository;
import com.gdcp.guangdongmuseumshop.repository.RoleRepository;

import com.gdcp.guangdongmuseumshop.service.FileService;
import com.gdcp.guangdongmuseumshop.service.UserService;
import com.gdcp.guangdongmuseumshop.config.DefaultsConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;
    private final DefaultsConfig defaultsConfig;
    private final RoleRepository roleRepository;

    @Value("${app.admin.register-code}")
    private String adminRegisterCode;

    @Override
    public UserProfileDTO getUserProfile(Long userId) {
        log.debug("开始获取用户信息, userId={}", userId);
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            log.debug("查询到用户信息: {}", user);
            UserProfileDTO profileDTO = new UserProfileDTO();
            profileDTO.setId(user.getId());
            profileDTO.setUsername(user.getUsername());
            profileDTO.setEmail(user.getEmail());
            profileDTO.setPhone(user.getPhone());
            profileDTO.setAvatarUrl(user.getAvatarUrl());
            log.debug("转换后的用户信息: {}", profileDTO);
            return profileDTO;
        } catch (Exception e) {
            log.error("获取用户信息失败:", e);
            throw new RuntimeException("获取用户信息失败", e);
        }
    }

    @Override
    public UserProfileDTO getUserProfile(String username) {
        log.debug("开始获取用户信息, username={}", username);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserException("用户不存在"));
        log.debug("查询到用户信息: {}", user);
        
        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setId(user.getId());
        profileDTO.setUsername(user.getUsername());
        profileDTO.setEmail(user.getEmail());
        profileDTO.setPhone(user.getPhone());
        profileDTO.setAvatarUrl(user.getAvatarUrl());
        
        log.debug("转换后的用户信息: {}", profileDTO);
        return profileDTO;
    }

    @Override
    public UserProfileDTO updateProfile(Long userId, UserProfileDTO profileDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("用户不存在"));
        
        // 只允许更新部分字段
        if (profileDTO.getEmail() != null) {
            user.setEmail(profileDTO.getEmail());
        }
        if (profileDTO.getPhone() != null) {
            user.setPhone(profileDTO.getPhone());
        }
        
        user = userRepository.save(user);
        
        // 返回更新后的用户信息
        UserProfileDTO updatedProfile = new UserProfileDTO();
        updatedProfile.setId(user.getId());
        updatedProfile.setUsername(user.getUsername());
        updatedProfile.setEmail(user.getEmail());
        updatedProfile.setPhone(user.getPhone());
        updatedProfile.setAvatarUrl(user.getAvatarUrl());
        
        return updatedProfile;
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, PasswordUpdateDTO passwordDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OrderException("用户不存在"));

        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
            throw new OrderException("原密码不正确");
        }

        if (!passwordDTO.getNewPassword().equals(passwordDTO.getConfirmPassword())) {
            throw new OrderException("两次输入的密码不一致");
        }

        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public String updateAvatar(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OrderException("用户不存在"));

        if (user.getAvatarUrl() != null) {
            fileService.deleteFile(user.getAvatarUrl());
        }

        try {
            String avatarUrl = fileService.uploadFile(file, "avatars");
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);
            return avatarUrl;
        } catch (Exception e) {
            throw new RuntimeException("头像上传失败", e);
        }
    }

    @Override
    public Page<UserProfileDTO> searchUsers(UserSearchDTO searchDTO, Pageable pageable) {
        log.info("执行用户搜索");
        log.debug("搜索条件: {}", searchDTO);
        log.debug("分页参数: {}", pageable);

        try {
            log.debug("开始数据库查询");
            Page<User> users = userRepository.search(
                searchDTO.getUsername(),
                searchDTO.getEmail(),
                searchDTO.getPhone(),
                searchDTO.getEnabled(),
                pageable
            );
            log.info("数据库查询完成，记录数: {}", users.getTotalElements());
            log.debug("查询到的用户: {}", users.getContent());

            Page<UserProfileDTO> result = users.map(this::convertToDTO);
            log.debug("转换后的DTO: {}", result.getContent());
            return result;
        } catch (Exception e) {
            log.error("用户搜索失败:", e);
            throw new RuntimeException("搜索用户失败", e);
        }
    }

    @Override
    @Transactional
    public void enableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("用户不存在"));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void disableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("用户不存在"));
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("用户不存在"));
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserDTO register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new UserException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new UserException("邮箱已被使用");
        }
        
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setEnabled(true);
        
        // 根据注册码判断是否为管理员注册
        Role role;
        if (StringUtils.hasText(registerDTO.getRegisterCode())) {
            if (!adminRegisterCode.equals(registerDTO.getRegisterCode())) {
                throw new UserException("注册码无效");
            }
            role = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new UserException("管理员角色不存在"));
        } else {
            role = roleRepository.findByName("USER")
                    .orElseThrow(() -> new UserException("用户角色不存在"));
        }
        
        user.getRoles().add(role);
        user = userRepository.save(user);
        
        return convertToUserDTO(user);
    }

    private UserProfileDTO convertToDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatarUrl(user.getAvatarUrl() != null ? 
            user.getAvatarUrl() : defaultsConfig.getAvatar());
        return dto;
    }

    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setEnabled(user.isEnabled());
        dto.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    @Override
    @Transactional
    public UserProfileDTO createUser(UserProfileDTO userDTO) {
        log.debug("开始创建用户: {}", userDTO);
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new UserException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userDTO.getEmail() != null && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new UserException("邮箱已被使用");
        }
        
        // 创建用户实体
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode("123456")); // 设置默认密码
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setEnabled(true);
        
        // 设置默认用户角色
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new UserException("用户角色不存在"));
        user.getRoles().add(userRole);
        
        // 保存用户
        user = userRepository.save(user);
        log.info("用户创建成功: {}", user.getUsername());
        
        return convertToDTO(user);
    }

    @Override
    @Transactional
    public Map<String, String> resetPassword(Long userId) {
        log.info("开始重置用户密码, userId: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("用户不存在"));
        
        String newPassword = "123456"; // 默认密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("密码重置成功");
        
        // 返回新密码
        Map<String, String> result = new HashMap<>();
        result.put("password", newPassword);
        return result;
    }

    @Override
    @Transactional
    public UserProfileDTO updateUser(Long userId, UserProfileDTO userDTO) {
        log.info("开始更新用户信息, userId: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("用户不存在"));

        // 如果修改了用户名，检查是否已存在
        if (!user.getUsername().equals(userDTO.getUsername()) && 
            userRepository.existsByUsername(userDTO.getUsername())) {
            throw new UserException("用户名已存在");
        }

        // 如果修改了邮箱，检查是否已存在
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail()) && 
            userRepository.existsByEmail(userDTO.getEmail())) {
            throw new UserException("邮箱已被使用");
        }

        // 更新用户信息
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        
        user = userRepository.save(user);
        log.info("用户信息更新成功");
        
        return convertToDTO(user);
    }

    @Override
    public List<UserProfileDTO> getAllUsers() {
        log.info("获取所有用户列表");
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> UserProfileDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .avatarUrl(user.getAvatarUrl())
                        .enabled(user.isEnabled())
                        .build())
                .collect(Collectors.toList());
    }
} 