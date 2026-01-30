package com.smartparking.api.controller.publicapi;

import com.smartparking.common.api.Result;
import com.smartparking.common.error.BusinessException;
import com.smartparking.service.ai.QwenChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public/v1/ai")
public class AiHealthController {
    private final QwenChatClient qwenChatClient;

    public AiHealthController(QwenChatClient qwenChatClient) {
        this.qwenChatClient = qwenChatClient;
    }

    @GetMapping("/health")
    public Result<Map<String, Object>> health(@RequestParam(value = "deep", defaultValue = "false") boolean deep) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("configured", qwenChatClient.isConfigured());
        data.put("baseUrl", qwenChatClient.getBaseUrl());
        data.put("model", qwenChatClient.getModel());

        if (!deep) {
            return Result.ok(data);
        }

        if (!qwenChatClient.isConfigured()) {
            data.put("ok", false);
            data.put("error", "AI API Key 未配置");
            return Result.ok(data);
        }

        try {
            long latencyMs = qwenChatClient.ping(Duration.ofSeconds(10));
            data.put("ok", true);
            data.put("latencyMs", latencyMs);
        } catch (BusinessException e) {
            data.put("ok", false);
            data.put("error", e.getMessage());
        }
        return Result.ok(data);
    }
}

