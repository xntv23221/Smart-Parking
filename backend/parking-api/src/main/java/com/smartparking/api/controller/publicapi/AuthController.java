package com.smartparking.api.controller.publicapi;

import com.smartparking.api.dto.publicapi.IssueTokenRequest;
import com.smartparking.api.dto.publicapi.LoginRequest;
import com.smartparking.common.api.Result;
import com.smartparking.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<Map<String, String>> register(@Valid @RequestBody IssueTokenRequest request) {
        String token = authService.register(request.getUsername(), request.getPassword(), request.getRole(), request.getPhone());
        return Result.ok(Map.of("token", token));
    }

    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword());
        return Result.ok(Map.of("token", token));
    }
}
