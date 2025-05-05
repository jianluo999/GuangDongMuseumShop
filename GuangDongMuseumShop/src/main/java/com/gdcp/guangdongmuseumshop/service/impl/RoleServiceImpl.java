package com.gdcp.guangdongmuseumshop.service.impl;

import com.gdcp.guangdongmuseumshop.dto.RoleDTO;
import com.gdcp.guangdongmuseumshop.dto.UserRoleDTO;
import com.gdcp.guangdongmuseumshop.entity.Role;
import com.gdcp.guangdongmuseumshop.exception.UserException;

import com.gdcp.guangdongmuseumshop.entity.User;
import com.gdcp.guangdongmuseumshop.repository.RoleRepository;
import com.gdcp.guangdongmuseumshop.repository.UserRepository;
import com.gdcp.guangdongmuseumshop.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO) {
        if (roleRepository.existsByName(roleDTO.getName())) {
            throw new UserException("角色名称已存在");
        }

        Role role = new Role();
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());
        role = roleRepository.save(role);
        
        return convertToDTO(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new UserException("角色不存在"));
        roleRepository.delete(role);
    }

    @Override
    @Transactional
    public void assignUserRoles(Long userId, UserRoleDTO userRoleDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("用户不存在"));

        Set<Role> roles = userRoleDTO.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new UserException("角色不存在: " + roleName)))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        userRepository.save(user);
    }

    @Override
    public List<RoleDTO> getUserRoles(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("用户不存在"));

        return user.getRoles().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private RoleDTO convertToDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        return dto;
    }
} 