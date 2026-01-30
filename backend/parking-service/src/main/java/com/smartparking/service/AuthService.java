package com.smartparking.service;

import com.smartparking.common.security.UserRole;

public interface AuthService {
    String register(String username, String rawPassword, UserRole role, String phone);

    String login(String username, String rawPassword);
}

