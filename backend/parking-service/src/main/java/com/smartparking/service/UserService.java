package com.smartparking.service;

import com.smartparking.domain.model.User;

public interface UserService {
    User getById(Long userId);
    User getByUsername(String username);
    void update(User user);
    void updateProfile(Long userId, String nickname, String email, String avatarUrl);
    void register(String username, String password, String phone, String email);
}
