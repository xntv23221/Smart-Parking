package com.smartparking.service.impl;

import com.smartparking.common.error.BusinessException;
import com.smartparking.common.error.ErrorCode;
import com.smartparking.common.security.UserRole;
import com.smartparking.dao.mapper.ManagerMapper;
import com.smartparking.dao.mapper.SystemAdminMapper;
import com.smartparking.dao.mapper.UserMapper;
import com.smartparking.domain.model.Manager;
import com.smartparking.domain.model.SystemAdmin;
import com.smartparking.domain.model.User;
import com.smartparking.manager.security.JwtService;
import com.smartparking.manager.security.PasswordService;
import com.smartparking.service.AuthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final SystemAdminMapper adminMapper;
    private final ManagerMapper managerMapper;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public AuthServiceImpl(UserMapper userMapper,
                           SystemAdminMapper adminMapper,
                           ManagerMapper managerMapper,
                           PasswordService passwordService,
                           JwtService jwtService) {
        this.userMapper = userMapper;
        this.adminMapper = adminMapper;
        this.managerMapper = managerMapper;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public String register(String username, String rawPassword, UserRole role, String phone) {
        if (role != UserRole.CLIENT) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅支持用户端注册");
        }

        if (phone == null || phone.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "手机号不能为空");
        }
        
        if (userMapper.selectByUsername(username) != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }
        if (userMapper.selectByPhone(phone) != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "手机号已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordService.hash(rawPassword));
        user.setPhone(phone.trim());
        user.setBalance(BigDecimal.ZERO);
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userMapper.insert(user);

        return jwtService.issueToken(user.getUserId(), UserRole.CLIENT);
    }

    @Override
    public String login(String username, String rawPassword) {
        // 1. Try User
        User user = userMapper.selectByUsername(username);
        if (user != null) {
            if (passwordService.matches(rawPassword, user.getPasswordHash())) {
                if (user.getStatus() != 1) {
                    throw new BusinessException(ErrorCode.FORBIDDEN, "Account disabled");
                }
                return jwtService.issueToken(user.getUserId(), UserRole.CLIENT);
            }
        }

        // 2. Try Admin
        SystemAdmin admin = adminMapper.selectByUsername(username);
        if (admin != null) {
            if (passwordService.matches(rawPassword, admin.getPasswordHash())) {
                return jwtService.issueToken(admin.getAdminId(), UserRole.ADMIN);
            }
        }

        // 3. Try Manager
        Manager manager = managerMapper.selectByUsername(username);
        if (manager != null) {
            if (passwordService.matches(rawPassword, manager.getPasswordHash())) {
                if (manager.getStatus() != 1) {
                    throw new BusinessException(ErrorCode.FORBIDDEN, "Account disabled");
                }
                return jwtService.issueToken(manager.getManagerId(), UserRole.MANAGER); // Assuming UserRole has MANAGER
            }
        }

        throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid credentials");
    }
}
