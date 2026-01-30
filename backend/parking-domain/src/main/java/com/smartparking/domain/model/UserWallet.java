package com.smartparking.domain.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserWallet {
    private Long walletId;
    private Long userId;
    private BigDecimal balance;
    private Integer version;
    private LocalDateTime updatedAt;
}
