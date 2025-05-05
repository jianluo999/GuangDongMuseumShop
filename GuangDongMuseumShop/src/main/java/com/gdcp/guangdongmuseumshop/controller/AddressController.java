package com.gdcp.guangdongmuseumshop.controller;

import com.gdcp.guangdongmuseumshop.common.Result;
import com.gdcp.guangdongmuseumshop.dto.AddressDTO;
import com.gdcp.guangdongmuseumshop.security.CustomUserDetails;
import com.gdcp.guangdongmuseumshop.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<AddressDTO> create(
            @Valid @RequestBody AddressDTO addressDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        return Result.success(addressService.create(userId, addressDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<AddressDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AddressDTO addressDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        return Result.success(addressService.update(userId, id, addressDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<?> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        addressService.delete(userId, id);
        return Result.success("地址删除成功");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<AddressDTO> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        return Result.success(addressService.getById(userId, id));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<List<AddressDTO>> getByUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        return Result.success(addressService.getByUser(userId));
    }

    @PutMapping("/{id}/default")
    @PreAuthorize("isAuthenticated()")
    public Result<AddressDTO> setDefault(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        return Result.success(addressService.setDefault(userId, id));
    }

    // 这个方法需要根据实际的UserDetails实现来获取用户ID
    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        if (userDetails instanceof CustomUserDetails) {
            return ((CustomUserDetails) userDetails).getId();
        }
        throw new RuntimeException("Invalid user details");
    }
} 