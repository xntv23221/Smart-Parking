package com.smartparking.domain.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ParkingSpot {
    private Long spotId;
    private Long lotId;
    private String spotNumber;
    private Integer spotType; // 1=Normal, 2=Disabled, 3=Charging
    private Boolean isOccupied;
    private LocalDateTime lastUpdated;
}
