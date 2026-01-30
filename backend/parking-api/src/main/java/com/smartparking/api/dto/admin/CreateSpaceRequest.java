package com.smartparking.api.dto.admin;

import com.smartparking.domain.enums.ParkingSpaceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateSpaceRequest {
    @NotBlank
    @Size(max = 64)
    private String code;

    @NotBlank
    @Size(max = 64)
    private String area;

    private ParkingSpaceStatus status;

    @NotBlank
    @Size(max = 32)
    private String type;
}

