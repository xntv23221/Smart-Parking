package com.smartparking.domain.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class User {
    private Long userId;
    private String username;
    private String passwordHash;
    private String phone;
    private String email;
    private String nickname;
    private String avatarUrl;
    private BigDecimal balance;
    private Integer status; // 0=Disabled, 1=Normal
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
