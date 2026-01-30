package com.smartparking.api.dto.client;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderIdRequest {
    @Positive
    private long orderId;
}

