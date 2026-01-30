package com.smartparking.api.dto.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AssistantChatRequest {
    @NotBlank
    @Size(max = 2000)
    private String message;
}

