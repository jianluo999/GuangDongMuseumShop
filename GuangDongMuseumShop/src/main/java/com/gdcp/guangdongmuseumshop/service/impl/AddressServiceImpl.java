package com.gdcp.guangdongmuseumshop.service.impl;

import com.gdcp.guangdongmuseumshop.dto.AddressDTO;
import com.gdcp.guangdongmuseumshop.entity.Address;
import com.gdcp.guangdongmuseumshop.exception.OrderException;

import com.gdcp.guangdongmuseumshop.entity.User;

import com.gdcp.guangdongmuseumshop.repository.AddressRepository;
import com.gdcp.guangdongmuseumshop.repository.UserRepository;
import com.gdcp.guangdongmuseumshop.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AddressDTO create(Long userId, AddressDTO addressDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OrderException("用户不存在"));

        Address address = new Address();
        updateAddressFromDTO(address, addressDTO);
        address.setUser(user);

        if (Boolean.TRUE.equals(addressDTO.getIsDefault())) {
            addressRepository.clearOtherDefaultAddress(userId, null);
        } else if (addressRepository.findByUserIdAndIsDefaultTrue(userId).isEmpty()) {
            // 如果是第一个地址，自动设为认
            address.setIsDefault(true);
        }

        address = addressRepository.save(address);
        return convertToDTO(address);
    }

    @Override
    @Transactional
    public AddressDTO update(Long userId, Long addressId, AddressDTO addressDTO) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new OrderException("地址不存在"));

        updateAddressFromDTO(address, addressDTO);

        if (Boolean.TRUE.equals(addressDTO.getIsDefault())) {
            addressRepository.clearOtherDefaultAddress(userId, addressId);
        }

        address = addressRepository.save(address);
        return convertToDTO(address);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new OrderException("地址不存在"));
        addressRepository.delete(address);
    }

    @Override
    public AddressDTO getById(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new OrderException("地址不存在"));
        return convertToDTO(address);
    }

    @Override
    public List<AddressDTO> getByUser(Long userId) {
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressDTO setDefault(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new OrderException("地址不存在"));

        addressRepository.clearOtherDefaultAddress(userId, addressId);
        address.setIsDefault(true);
        address = addressRepository.save(address);
        return convertToDTO(address);
    }

    private void updateAddressFromDTO(Address address, AddressDTO dto) {
        address.setReceiver(dto.getReceiver());
        address.setPhone(dto.getPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetailAddress(dto.getDetailAddress());
        address.setIsDefault(dto.getIsDefault() != null && dto.getIsDefault());
    }

    private AddressDTO convertToDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId());
        dto.setReceiver(address.getReceiver());
        dto.setPhone(address.getPhone());
        dto.setProvince(address.getProvince());
        dto.setCity(address.getCity());
        dto.setDistrict(address.getDistrict());
        dto.setDetailAddress(address.getDetailAddress());
        dto.setIsDefault(address.getIsDefault());
        return dto;
    }
} 