package com.smartparking.domain.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Manager {
    private Long managerId;
    private String username;
    private String passwordHash;
    private String realName;
    private String phone;
    private Integer status; // 0=Disabled, 1=Enabled
    private LocalDateTime createdAt;
}
