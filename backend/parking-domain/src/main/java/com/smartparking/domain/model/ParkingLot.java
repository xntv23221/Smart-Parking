package com.smartparking.domain.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ParkingLot {
    private Long lotId;
    private String name;
    private String address;
    private Integer totalSpots;
    private Integer availableSpots;
    private Long managerId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer status; // 0=Closed, 1=Open
    private Integer type; // 0=Public, 1=Commercial
    private LocalDateTime createdAt;
}
