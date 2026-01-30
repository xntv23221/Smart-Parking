package com.smartparking.api.controller.client;

import com.smartparking.common.api.Result;
import com.smartparking.common.security.UserContextHolder;
import com.smartparking.domain.model.Message;
import com.smartparking.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/client/v1/messages")
public class ClientMessageController {

    private final MessageService messageService;

    public ClientMessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public Result<List<Message>> list() {
        Long userId = UserContextHolder.get().getUserId();
        return Result.ok(messageService.getAllMyMessages(userId, "user"));
    }

    @GetMapping("/conversation")
    public Result<List<Message>> conversation(
            @RequestParam(name = "targetId", defaultValue = "0") Long targetId, 
            @RequestParam(name = "targetRole", defaultValue = "admin") String targetRole) {
        Long userId = UserContextHolder.get().getUserId();
        return Result.ok(messageService.getConversation(userId, "user", targetId, targetRole));
    }

    @PostMapping("/send")
    public Result<Void> send(@RequestBody Map<String, Object> params) {
        Long userId = UserContextHolder.get().getUserId();
        String content = (String) params.get("content");
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("内容不能为空");
        }
        
        Long receiverId = 0L;
        String receiverRole = "admin";
        
        if (params.containsKey("receiverId")) {
            // Fix: Handle different number types (Integer/Long) from JSON
            Object rId = params.get("receiverId");
            if (rId instanceof Number) {
                receiverId = ((Number) rId).longValue();
            } else if (rId instanceof String) {
                try {
                    receiverId = Long.parseLong((String) rId);
                } catch (NumberFormatException e) {
                     // ignore, use default 0L
                }
            }
        }
        if (params.containsKey("receiverRole")) {
            receiverRole = (String) params.get("receiverRole");
        }
        
        messageService.send(userId, "user", receiverId, receiverRole, content, "private");
        return Result.ok(null);
    }

    @PostMapping("/{id}/read")
    public Result<Void> read(@PathVariable("id") Long id) {
        messageService.read(id);
        return Result.ok(null);
    }
    
    @PostMapping("/read-all")
    public Result<Void> readAll() {
        Long userId = UserContextHolder.get().getUserId();
        messageService.readAll(userId, "user");
        return Result.ok(null);
    }
}
