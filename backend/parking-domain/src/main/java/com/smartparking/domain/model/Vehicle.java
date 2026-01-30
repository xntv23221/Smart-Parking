package com.smartparking.domain.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Vehicle {
    private Long vehicleId;
    private Long userId;
    private String plateNumber;
    private String brand;
    private String model;
    private String color;
    private Integer vehicleType; // 1=Small, 2=Large, 3=New Energy
    private Boolean isDefault;
    private LocalDateTime createdAt;
}
