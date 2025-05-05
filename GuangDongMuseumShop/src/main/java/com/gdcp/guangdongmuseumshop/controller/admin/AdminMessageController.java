package com.gdcp.guangdongmuseumshop.controller.admin;

import com.gdcp.guangdongmuseumshop.dto.MessageDTO;
import com.gdcp.guangdongmuseumshop.dto.response.ApiResponse;
import com.gdcp.guangdongmuseumshop.enums.MessageType;
import com.gdcp.guangdongmuseumshop.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/messages")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMessageController {

    private final MessageService messageService;

    @GetMapping
    public ApiResponse<Page<MessageDTO>> getMessages(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isRead,
            Pageable pageable) {
        MessageType messageType = type != null ? MessageType.valueOf(type) : null;
        Page<MessageDTO> messages = messageService.getAdminMessages(username, messageType, isRead, pageable);
        return ApiResponse.ok(messages);
    }

    @PostMapping("/send")
    public ApiResponse<Void> sendSystemMessage(@RequestBody MessageDTO messageDTO) {
        messageService.createSystemMessage(messageDTO.getUserId(), messageDTO.getContent());
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return ApiResponse.ok();
    }
} 