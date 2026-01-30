package com.smartparking.service.impl;

import com.smartparking.dao.mapper.AuditRequestMapper;
import com.smartparking.dao.mapper.UserMapper;
import com.smartparking.domain.model.AuditRequest;
import com.smartparking.domain.model.User;
import com.smartparking.service.AuditService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditServiceImpl implements AuditService {

    private final AuditRequestMapper auditRequestMapper;
    private final UserMapper userMapper;

    public AuditServiceImpl(AuditRequestMapper auditRequestMapper, UserMapper userMapper) {
        this.auditRequestMapper = auditRequestMapper;
        this.userMapper = userMapper;
    }

    @Override
    public void createPhoneChangeRequest(Long userId, String newPhone) {
        User user = userMapper.selectById(userId);
        if (user == null) return;

        AuditRequest request = new AuditRequest();
        request.setUserId(userId);
        request.setType("phone_change");
        request.setOldValue(user.getPhone());
        request.setNewValue(newPhone);
        request.setStatus(0); // Pending
        request.setCreatedAt(LocalDateTime.now());
        auditRequestMapper.insert(request);
    }

    @Override
    public List<AuditRequest> getMyRequests(Long userId) {
        return auditRequestMapper.selectByUserId(userId);
    }
}
