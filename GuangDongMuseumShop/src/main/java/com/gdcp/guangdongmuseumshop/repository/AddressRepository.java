package com.gdcp.guangdongmuseumshop.repository;

import com.gdcp.guangdongmuseumshop.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);
    
    Optional<Address> findByIdAndUserId(Long id, Long userId);
    
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
    
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = ?1 AND a.id != ?2")
    void clearOtherDefaultAddress(Long userId, Long addressId);
} 