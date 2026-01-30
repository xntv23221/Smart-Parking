package com.smartparking.api.controller.admin;

import com.smartparking.common.api.Result;
import com.smartparking.common.security.UserContextHolder;
import com.smartparking.common.security.UserRole;
import com.smartparking.domain.model.Message;
import com.smartparking.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/v1/messages")
public class AdminMessageController {

    private final MessageService messageService;

    public AdminMessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public Result<List<Message>> list() {
        Long userId = UserContextHolder.get().getUserId();
        UserRole role = UserContextHolder.get().getRole();
        
        List<Message> messages = new ArrayList<>();
        
        if (role == UserRole.ADMIN) {
            // Admins can see system messages (ID 0) and their own messages
            // Use getAllMyMessages to include sent messages too
            messages.addAll(messageService.getAllMyMessages(0L, "admin"));
            if (userId != 0L) {
                messages.addAll(messageService.getAllMyMessages(userId, "admin"));
            }
        } else if (role == UserRole.MANAGER) {
            messages.addAll(messageService.getAllMyMessages(userId, "manager"));
        }
        
        return Result.ok(messages);
    }

    @GetMapping("/user/{targetId}")
    public Result<List<Message>> conversation(@PathVariable("targetId") Long targetId) {
        Long myId = UserContextHolder.get().getUserId();
        UserRole myRole = UserContextHolder.get().getRole();
        String myRoleStr = myRole == UserRole.ADMIN ? "admin" : "manager";
        
        // If Admin, use 0L as sender ID for system messages context
        if (myRole == UserRole.ADMIN) {
            List<Message> messages = messageService.getConversation(0L, "admin", targetId, "user");
            
            // Also include messages sent specifically by/to this admin user (if any)
            if (myId != 0L) {
                List<Message> personal = messageService.getConversation(myId, "admin", targetId, "user");
                if (!personal.isEmpty()) {
                    messages.addAll(personal);
                    messages.sort(Comparator.comparing(Message::getCreatedAt));
                }
            }
            return Result.ok(messages);
        }
        
        return Result.ok(messageService.getConversation(myId, myRoleStr, targetId, "user"));
    }

    @PostMapping("/reply")
    public Result<Void> reply(@RequestBody Map<String, Object> params) {
        // Fix: Handle different number types (Integer/Long) from JSON
        Object uId = params.get("userId");
        Long targetId = 0L;
        if (uId instanceof Number) {
            targetId = ((Number) uId).longValue();
        } else if (uId instanceof String) {
            try {
                targetId = Long.parseLong((String) uId);
            } catch (NumberFormatException e) {
                 throw new IllegalArgumentException("Invalid userId format");
            }
        } else {
             throw new IllegalArgumentException("userId is required");
        }

        String content = (String) params.get("content");
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("内容不能为空");
        }
        
        Long myId = UserContextHolder.get().getUserId();
        UserRole myRole = UserContextHolder.get().getRole();
        String myRoleStr = myRole == UserRole.ADMIN ? "admin" : "manager";
        
        // If Admin, send as System (0L)
        if (myRole == UserRole.ADMIN) {
            messageService.send(0L, "admin", targetId, "user", content, "private");
        } else {
            messageService.send(myId, myRoleStr, targetId, "user", content, "private");
        }
        
        return Result.ok(null);
    }

    @PostMapping("/read/{targetId}")
    public Result<Void> read(@PathVariable Long targetId) {
        Long myId = UserContextHolder.get().getUserId();
        UserRole myRole = UserContextHolder.get().getRole();
        String myRoleStr = myRole == UserRole.ADMIN ? "admin" : "manager";
        
        if (myRole == UserRole.ADMIN) {
            messageService.readConversation(0L, "admin", targetId, "user");
        } else {
            messageService.readConversation(myId, myRoleStr, targetId, "user");
        }
        
        return Result.ok(null);
    }
}
