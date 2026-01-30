package com.smartparking.api.controller.client;

import com.smartparking.api.dto.client.AssistantChatRequest;
import com.smartparking.api.dto.client.AssistantChatResponse;
import com.smartparking.common.api.Result;
import com.smartparking.service.AiAssistantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client/v1/assistant")
public class ClientAssistantController {
    private final AiAssistantService aiAssistantService;

    /**
     * 提供客户端 AI 助手问答接口（需要 CLIENT Token）。
     */
    public ClientAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    /**
     * 发送用户问题并返回 AI 生成的回答文本。
     */
    @PostMapping("/chat")
    public Result<AssistantChatResponse> chat(@Valid @RequestBody AssistantChatRequest request) {
        String answer = aiAssistantService.ask(request.getMessage());
        return Result.ok(new AssistantChatResponse(answer));
    }
}

