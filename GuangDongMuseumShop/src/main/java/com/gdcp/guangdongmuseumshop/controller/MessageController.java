package com.gdcp.guangdongmuseumshop.controller;

import com.gdcp.guangdongmuseumshop.common.Result;
import com.gdcp.guangdongmuseumshop.dto.MessageDTO;
import com.gdcp.guangdongmuseumshop.service.MessageService;
import com.gdcp.guangdongmuseumshop.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public Result<Page<MessageDTO>> getUserMessages(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.debug("获取用户消息列表, userId: {}, page: {}, size: {}", 
            userId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<MessageDTO> messages = messageService.getUserMessages(userId, pageable);
        log.debug("获取到消息数量: {}, 总数: {}", 
            messages.getContent().size(), 
            messages.getTotalElements());
            
        return Result.success(messages);
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.debug("获取未读消息数量, userId: {}", userId);
        Long count = messageService.getUnreadCount(userId);
        log.debug("未读消息数量: {}", count);
        return Result.success(count);
    }

    @PostMapping("/mark-all-read")
    @Transactional
    public Result<Void> markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.debug("标记所有消息为已读, userId: {}", userId);
        messageService.markAllAsRead(userId);
        return Result.success(null);
    }

    @PostMapping("/{messageId}/mark-read")
    @Transactional
    public Result<Void> markAsRead(@PathVariable Long messageId) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.debug("标记消息为已读, messageId: {}, userId: {}", messageId, userId);
        messageService.markAsRead(messageId, userId);
        return Result.success(null);
    }
} 