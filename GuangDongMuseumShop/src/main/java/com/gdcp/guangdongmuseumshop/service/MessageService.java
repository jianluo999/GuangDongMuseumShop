package com.gdcp.guangdongmuseumshop.service;

import com.gdcp.guangdongmuseumshop.dto.MessageDTO;
import com.gdcp.guangdongmuseumshop.enums.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {
    Page<MessageDTO> getUserMessages(Long userId, Pageable pageable);
    
    Page<MessageDTO> getAdminMessages(String username, MessageType type, Boolean isRead, Pageable pageable);
    
    long getUnreadCount(Long userId);
    
    void markAllAsRead(Long userId);
    
    void markAsRead(Long messageId, Long userId);
    
    void createOrderMessage(Long userId, Long orderId, String content);
    
    void createSystemMessage(Long userId, String content);

    void createOrderStatusMessage(Long userId, Long orderId, String orderNo, String status);

    // 管理员相关方法
    void deleteMessage(Long messageId);
} 