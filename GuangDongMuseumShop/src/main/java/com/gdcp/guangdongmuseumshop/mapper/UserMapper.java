package com.gdcp.guangdongmuseumshop.mapper;

import com.gdcp.guangdongmuseumshop.dto.UserDTO;
import com.gdcp.guangdongmuseumshop.entity.User;
import com.gdcp.guangdongmuseumshop.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStrings")
    UserDTO toDTO(User user);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", source = "roles", qualifiedByName = "stringsToRoles")
    User toEntity(UserDTO userDTO);

    @Named("rolesToStrings")
    default Set<String> rolesToStrings(Set<Role> roles) {
        if (roles == null) return new HashSet<>();
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    @Named("stringsToRoles")
    default Set<Role> stringsToRoles(Set<String> roleNames) {
        if (roleNames == null) return new HashSet<>();
        return roleNames.stream()
                .map(name -> {
                    Role role = new Role();
                    role.setName(name);
                    return role;
                })
                .collect(Collectors.toSet());
    }
} 