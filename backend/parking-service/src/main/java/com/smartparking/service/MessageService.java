package com.smartparking.service;

import com.smartparking.domain.model.Message;
import java.util.List;

public interface MessageService {
    void send(Long senderId, String senderRole, Long receiverId, String receiverRole, String content, String type);

    List<Message> getMyMessages(Long receiverId, String receiverRole);
    
    /**
     * Get all messages related to user (sent or received)
     */
    List<Message> getAllMyMessages(Long userId, String userRole);

    List<Message> getConversation(Long userId, String userRole, Long otherId, String otherRole);
    void read(Long messageId);
    void readAll(Long receiverId, String receiverRole);
    void readConversation(Long receiverId, String receiverRole, Long senderId, String senderRole);
}
