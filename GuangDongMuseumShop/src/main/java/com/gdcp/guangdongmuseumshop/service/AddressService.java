package com.gdcp.guangdongmuseumshop.service;

import com.gdcp.guangdongmuseumshop.dto.AddressDTO;

import java.util.List;

public interface AddressService {
    AddressDTO create(Long userId, AddressDTO addressDTO);
    AddressDTO update(Long userId, Long addressId, AddressDTO addressDTO);
    void delete(Long userId, Long addressId);
    AddressDTO getById(Long userId, Long addressId);
    List<AddressDTO> getByUser(Long userId);
    AddressDTO setDefault(Long userId, Long addressId);
} 