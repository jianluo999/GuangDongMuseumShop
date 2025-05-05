package com.gdcp.guangdongmuseumshop.dto;

import com.gdcp.guangdongmuseumshop.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private Long userId;
    private String username;
    private String title;
    private String content;
    private MessageType type;
    private boolean isRead;
    private String relatedId;
    private LocalDateTime createdAt;
} 