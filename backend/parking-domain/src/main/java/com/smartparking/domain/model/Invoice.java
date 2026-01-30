package com.smartparking.domain.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Invoice {
    private Long invoiceId;
    private Long userId;
    private BigDecimal amount;
    private String title;
    private String taxNo;
    private Integer status; // 0=Pending, 1=Issued
    private LocalDateTime createdAt;
}
