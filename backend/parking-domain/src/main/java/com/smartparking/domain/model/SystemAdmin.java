package com.smartparking.domain.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SystemAdmin {
    private Long adminId;
    private String username;
    private String passwordHash;
    private String realName;
    private String phone;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}
