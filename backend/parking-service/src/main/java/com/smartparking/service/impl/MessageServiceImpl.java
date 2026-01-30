package com.smartparking.service.impl;

import com.smartparking.dao.mapper.MessageMapper;
import com.smartparking.domain.model.Message;
import com.smartparking.service.MessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    public MessageServiceImpl(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    @Override
    public void send(Long senderId, String senderRole, Long receiverId, String receiverRole, String content, String type) {
        Message msg = new Message();
        msg.setSenderId(senderId);
        msg.setSenderRole(senderRole);
        msg.setReceiverId(receiverId);
        msg.setReceiverRole(receiverRole);
        msg.setContent(content);
        msg.setType(type);
        msg.setIsRead(false);
        msg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(msg);
    }

    @Override
    public List<Message> getMyMessages(Long receiverId, String receiverRole) {
        return messageMapper.selectByReceiver(receiverId, receiverRole);
    }

    @Override
    public List<Message> getAllMyMessages(Long userId, String userRole) {
        return messageMapper.selectRelatedMessages(userId, userRole);
    }

    @Override
    public List<Message> getConversation(Long userId, String userRole, Long otherId, String otherRole) {
        return messageMapper.selectConversation(userId, userRole, otherId, otherRole);
    }

    @Override
    public void read(Long messageId) {
        messageMapper.updateReadStatus(messageId);
    }

    @Override
    public void readAll(Long receiverId, String receiverRole) {
        messageMapper.markAllRead(receiverId, receiverRole);
    }

    @Override
    public void readConversation(Long receiverId, String receiverRole, Long senderId, String senderRole) {
        messageMapper.markConversationRead(receiverId, receiverRole, senderId, senderRole);
    }
}
