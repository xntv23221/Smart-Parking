package com.smartparking.api.controller.client;

import com.smartparking.common.api.Result;
import com.smartparking.common.security.UserContextHolder;
import com.smartparking.domain.model.AuditRequest;
import com.smartparking.domain.model.User;
import com.smartparking.service.AuditService;
import com.smartparking.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/client/v1/user")
public class ClientUserController {

    private final UserService userService;
    private final AuditService auditService;

    public ClientUserController(UserService userService, AuditService auditService) {
        this.userService = userService;
        this.auditService = auditService;
    }

    @GetMapping("/profile")
    public Result<User> getProfile() {
        Long userId = UserContextHolder.get().getUserId();
        User user = userService.getById(userId);
        user.setPasswordHash(null); // Hide sensitive info
        return Result.ok(user);
    }

    @PostMapping("/profile")
    public Result<Void> updateProfile(@RequestBody Map<String, String> params) {
        Long userId = UserContextHolder.get().getUserId();
        String nickname = params.get("nickname");
        String email = params.get("email");
        String avatarUrl = params.get("avatarUrl");
        userService.updateProfile(userId, nickname, email, avatarUrl);
        return Result.ok(null);
    }

    @PostMapping("/phone/change-request")
    public Result<Void> requestPhoneChange(@RequestBody Map<String, String> params) {
        Long userId = UserContextHolder.get().getUserId();
        String newPhone = params.get("newPhone");
        // In real world, verify SMS code here
        auditService.createPhoneChangeRequest(userId, newPhone);
        return Result.ok(null);
    }
    
    @GetMapping("/audit-requests")
    public Result<List<AuditRequest>> getAuditRequests() {
        Long userId = UserContextHolder.get().getUserId();
        return Result.ok(auditService.getMyRequests(userId));
    }
}
