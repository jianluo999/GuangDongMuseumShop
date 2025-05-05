package com.gdcp.guangdongmuseumshop.config;

import com.gdcp.guangdongmuseumshop.entity.User;
import com.gdcp.guangdongmuseumshop.entity.Role;
import com.gdcp.guangdongmuseumshop.repository.UserRepository;
import com.gdcp.guangdongmuseumshop.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.HashSet;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        // 确保角色名称为 ROLE_ADMIN
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
            .orElseGet(() -> {
                Role role = new Role();
                role.setName("ROLE_ADMIN");
                return roleRepository.save(role);
            });
            
        // ... 其他初始化代码
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("开始初始化数据...");
        
        // 1. 创建或获取角色
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
            .orElseGet(() -> {
                log.info("Creating ROLE_ADMIN role");
                Role newRole = new Role();
                newRole.setName("ROLE_ADMIN");
                newRole.setDescription("管理员角色");
                return roleRepository.save(newRole);
            });

        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseGet(() -> {
                log.info("Creating ROLE_USER role");
                Role newRole = new Role();
                newRole.setName("ROLE_USER");
                newRole.setDescription("用户角色");
                return roleRepository.save(newRole);
            });

        // 2. 检查管理员账号
        User admin = userRepository.findByUsername("admin")
            .orElseGet(() -> {
                log.info("Creating admin user");
                return User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@example.com")
                    .enabled(true)
                    .roles(new HashSet<>())
                    .build();
            });
        
        // 3. 确保管理员有正确的角色
        if (!admin.getRoles().contains(adminRole)) {
            admin.getRoles().add(adminRole);
            admin.getRoles().add(userRole);  // 同时添加用户角色
            admin = userRepository.save(admin);
            log.info("Updated admin user roles. User ID: {}", admin.getId());
        }
        
        // 4. 验证角色
        User verifiedAdmin = userRepository.findByUsername("admin")
            .orElseThrow(() -> new RuntimeException("Admin user not found"));
        log.debug("Verified admin roles: {}", 
            verifiedAdmin.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(", ")));
        
        log.info("数据初始化完成");
    }
} 