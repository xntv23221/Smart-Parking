package com.smartparking.domain.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditRequest {
    private Long requestId;
    private Long userId;
    private String type; // phone_change
    private String oldValue;
    private String newValue;
    private Integer status; // 0=Pending, 1=Approved, 2=Rejected
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime handledAt;
    private Long handlerId;
}
