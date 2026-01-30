package com.smartparking.domain.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletLog {
    private Long logId;
    private Long walletId;
    private BigDecimal amount;
    private Integer type; // 1=Recharge, 2=Payment, 3=Refund
    private BigDecimal balanceAfter;
    private Long orderId;
    private String remark;
    private LocalDateTime createdAt;
}
