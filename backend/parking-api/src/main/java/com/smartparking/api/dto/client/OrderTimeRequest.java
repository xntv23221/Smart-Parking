package com.smartparking.api.dto.client;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderTimeRequest {
    @Positive
    private long orderId;

    @NotNull
    private LocalDateTime time;
}

