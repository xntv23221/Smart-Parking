package com.smartparking.service.impl;

import com.smartparking.common.error.BusinessException;
import com.smartparking.common.error.ErrorCode;
import com.smartparking.dao.mapper.UserMapper;
import com.smartparking.domain.model.User;
import com.smartparking.manager.security.PasswordService;
import com.smartparking.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordService passwordService;

    public UserServiceImpl(UserMapper userMapper, PasswordService passwordService) {
        this.userMapper = userMapper;
        this.passwordService = passwordService;
    }

    @Override
    public User getById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public void update(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
    }

    @Override
    public void updateProfile(Long userId, String nickname, String email, String avatarUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "User not found");
        }
        if (nickname != null) user.setNickname(nickname);
        if (email != null) user.setEmail(email);
        if (avatarUrl != null) user.setAvatarUrl(avatarUrl);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
    }

    @Override
    @Transactional
    public void register(String username, String password, String phone, String email) {
        if (userMapper.selectByUsername(username) != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "Username already exists");
        }
        if (userMapper.selectByPhone(phone) != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "Phone already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordService.hash(password));
        user.setPhone(phone);
        user.setEmail(email);
        user.setBalance(BigDecimal.ZERO);
        user.setStatus(1); // Normal
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
    }
}
