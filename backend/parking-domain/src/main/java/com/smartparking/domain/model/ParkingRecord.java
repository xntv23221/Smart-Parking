package com.smartparking.domain.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ParkingRecord {
    private Long recordId;
    private Long userId;
    private Long vehicleId;
    private Long lotId;
    private Long spotId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Integer durationMinutes;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private Integer status; // 0=BOOKED, 1=PARKED, 2=COMPLETED, 3=CANCELLED
    private Integer paymentStatus; // 0=Unpaid, 1=Paid, 2=Partial
    private String paymentMethod;
    private LocalDateTime createdAt;
}
