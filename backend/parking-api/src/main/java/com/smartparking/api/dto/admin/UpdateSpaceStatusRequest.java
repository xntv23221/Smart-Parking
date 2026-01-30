package com.smartparking.api.dto.admin;

import com.smartparking.domain.enums.ParkingSpaceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSpaceStatusRequest {
    @NotNull
    private ParkingSpaceStatus status;
}

