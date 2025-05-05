package com.gdcp.guangdongmuseumshop.service;

import com.gdcp.guangdongmuseumshop.dto.RoleDTO;
import com.gdcp.guangdongmuseumshop.dto.UserRoleDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RoleService {
    List<RoleDTO> getAllRoles();
    RoleDTO createRole(RoleDTO roleDTO);
    void deleteRole(Long roleId);
    void assignUserRoles(Long userId, UserRoleDTO userRoleDTO);
    List<RoleDTO> getUserRoles(Long userId);
} 