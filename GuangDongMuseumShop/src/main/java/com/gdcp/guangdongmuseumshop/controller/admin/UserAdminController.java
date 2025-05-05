package com.gdcp.guangdongmuseumshop.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ROLE_USER')")
@RequiredArgsConstructor
public class UserAdminController {
    // ...
} 