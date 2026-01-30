package com.smartparking.domain.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Message {
    private Long messageId;
    private Long senderId;
    private String senderRole; // admin, manager, system
    private Long receiverId;
    private String receiverRole; // user, manager
    private String content;
    private String type; // system, parking, private
    private Boolean isRead;
    private LocalDateTime createdAt;
}
