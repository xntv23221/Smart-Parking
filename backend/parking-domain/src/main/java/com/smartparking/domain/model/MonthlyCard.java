package com.smartparking.domain.model;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MonthlyCard {
    private Long cardId;
    private Long userId;
    private Long lotId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String plateNumber;
    private Integer status; // 1=Active, 0=Expired
    private LocalDateTime createdAt;
}
