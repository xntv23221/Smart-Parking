package com.smartparking.service;

import com.smartparking.domain.model.AuditRequest;
import java.util.List;

public interface AuditService {
    void createPhoneChangeRequest(Long userId, String newPhone);
    List<AuditRequest> getMyRequests(Long userId);
    // Methods for admin to handle requests will be added later or in admin module
}
