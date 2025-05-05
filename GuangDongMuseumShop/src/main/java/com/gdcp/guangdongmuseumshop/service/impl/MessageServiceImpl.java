package com.gdcp.guangdongmuseumshop.service.impl;

import com.gdcp.guangdongmuseumshop.dto.MessageDTO;
import com.gdcp.guangdongmuseumshop.entity.Message;
import com.gdcp.guangdongmuseumshop.entity.User;
import com.gdcp.guangdongmuseumshop.enums.MessageType;
import com.gdcp.guangdongmuseumshop.exception.ResourceNotFoundException;
import com.gdcp.guangdongmuseumshop.exception.UnauthorizedException;
import com.gdcp.guangdongmuseumshop.repository.MessageRepository;
import com.gdcp.guangdongmuseumshop.repository.UserRepository;
import com.gdcp.guangdongmuseumshop.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Override
    public Page<MessageDTO> getUserMessages(Long userId, Pageable pageable) {
        log.info("开始获取用户消息列表, userId: {}, page: {}, size: {}", 
            userId, pageable.getPageNumber(), pageable.getPageSize());
            
        // 验证用户是否存在
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
            
        Page<Message> messages = messageRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        log.debug("查询到原始消息列表: {}", messages.getContent());
        
        Page<MessageDTO> messageDTOs = messages.map(this::convertToDTO);
        log.info("转换后的消息列表大小: {}, 总数: {}", 
            messageDTOs.getContent().size(), 
            messageDTOs.getTotalElements());
        
        // 记录每条消息的状态
        messageDTOs.getContent().forEach(dto -> 
            log.debug("消息[{}]状态: {}", dto.getId(), dto.isRead()));
            
        return messageDTOs;
    }

    @Override
    public long getUnreadCount(Long userId) {
        log.debug("获取未读消息数量, userId: {}", userId);
        long count = messageRepository.countByUserIdAndIsReadFalse(userId);
        log.debug("未读消息数量: {}", count);
        return count;
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        log.debug("标记所有消息为已读, userId: {}", userId);
        
        // 获取用户所有未读消息
        List<Message> unreadMessages = messageRepository.findByUserIdAndIsReadFalse(userId);
        
        // 更新所有消息状态
        for (Message message : unreadMessages) {
            message.setRead(true);
        }
        messageRepository.saveAllAndFlush(unreadMessages);
        
        log.debug("已标记 {} 条消息为已读", unreadMessages.size());
    }

    @Override
    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        log.info("开始标记消息已读, messageId: {}, userId: {}", messageId, userId);
        
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("消息不存在"));
        log.debug("查询到消息: {}", message);
            
        if (!message.getUser().getId().equals(userId)) {
            log.warn("用户无权操作此消息, 消息所属用户: {}, 当前用户: {}", message.getUser().getId(), userId);
            throw new UnauthorizedException("无权操作此消息");
        }
        
        // 记录更新前状态
        boolean oldState = message.isRead();
        log.info("消息[{}]更新前状态: {}", messageId, oldState);
        
        // 直接修改实体状态
        message.setRead(true);
        Message savedMessage = messageRepository.saveAndFlush(message);
        
        log.info("消息[{}]状态已更新: {} -> {}", messageId, oldState, savedMessage.isRead());
    }

    @Override
    @Transactional
    public void createOrderMessage(Long userId, Long orderId, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Message message = Message.builder()
                .user(user)
                .title("订单通知")
                .content(content)
                .type(MessageType.ORDER)
                .isRead(false)
                .relatedId(String.valueOf(orderId))
                .build();

        messageRepository.save(message);
    }

    @Override
    @Transactional
    public void createSystemMessage(Long userId, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Message message = Message.builder()
                .user(user)
                .title("系统通知")
                .content(content)
                .type(MessageType.SYSTEM)
                .isRead(false)
                .build();

        messageRepository.save(message);
    }

    @Override
    @Transactional
    public void createOrderStatusMessage(Long userId, Long orderId, String orderNo, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String content = String.format("您的订单 %s %s", orderNo, getStatusDescription(status));
        
        Message message = Message.builder()
                .user(user)
                .title("订单状态更新")
                .content(content)
                .type(MessageType.ORDER_STATUS)
                .isRead(false)
                .relatedId(String.valueOf(orderId))
                .build();

        messageRepository.save(message);
    }

    private String getStatusDescription(String status) {
        return switch (status) {
            case "PENDING" -> "已创建，等待支付";
            case "PAID" -> "已支付，等待发货";
            case "SHIPPED" -> "已发货，请注意查收";
            case "COMPLETED" -> "已完成，欢迎评价";
            case "CANCELLED" -> "已取消";
            case "REFUNDING" -> "退款申请已提交";
            case "REFUNDED" -> "退款已完成";
            default -> "状态已更新：" + status;
        };
    }

    @Override
    public Page<MessageDTO> getAdminMessages(String username, MessageType type, Boolean isRead, Pageable pageable) {
        Specification<Message> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (username != null && !username.isEmpty()) {
                predicates.add(cb.like(root.get("user").get("username"), "%" + username + "%"));
            }
            
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            
            if (isRead != null) {
                predicates.add(cb.equal(root.get("isRead"), isRead));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        return messageRepository.findAll(spec, pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("消息不存在"));
        messageRepository.delete(message);
    }

    private MessageDTO convertToDTO(Message message) {
        if (message == null) {
            log.warn("尝试转换null消息为DTO");
            return null;
        }
        
        log.debug("开始转换消息为DTO, messageId: {}, type: {}, isRead: {}", 
            message.getId(), message.getType(), message.isRead());
            
        MessageDTO dto = MessageDTO.builder()
                .id(message.getId())
                .userId(message.getUser().getId())
                .username(message.getUser().getUsername())
                .title(message.getTitle())
                .content(message.getContent())
                .type(message.getType())
                .isRead(message.isRead())
                .relatedId(message.getRelatedId())
                .createdAt(message.getCreatedAt())
                .build();
                
        log.debug("消息转换完成, DTO: {}", dto);
        return dto;
    }
} 