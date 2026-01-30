package com.smartparking.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartparking.common.error.BusinessException;
import com.smartparking.common.error.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLHandshakeException;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class QwenChatClient {
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String baseUrl;
    private final String model;
    private final String apiKey;

    /**
     * 创建一个基于 OpenAI Compatible API 的 Qwen 调用客户端。
     */
    public QwenChatClient(ObjectMapper objectMapper,
                          @Value("${ai.qwen.baseUrl:https://dashscope.aliyuncs.com/compatible-mode/v1}") String baseUrl,
                          @Value("${ai.qwen.model:qwen3-max}") String model,
                          @Value("${ai.qwen.apiKey:}") String apiKey) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.baseUrl = stripTrailingSlash(baseUrl);
        this.model = model;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public boolean isConfigured() {
        return !apiKey.isBlank();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getModel() {
        return model;
    }

    public long ping(Duration timeout) {
        if (apiKey.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "AI API Key 未配置");
        }
        long start = System.nanoTime();
        Map<String, Object> body = new LinkedHashMap<>(buildPayload("", "ping"));
        body.put("temperature", 0.0);
        body.put("max_tokens", 1);

        HttpResponse<String> response = sendChatRequest(body, timeout == null ? Duration.ofSeconds(10) : timeout);
        parseAnswerUnchecked(response.body());
        return (System.nanoTime() - start) / 1_000_000L;
    }

    /**
     * 发送 system+user 的对话并返回模型生成的文本。
     */
    public String chat(String systemPrompt, String userMessage) {
        if (apiKey.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "AI API Key 未配置");
        }
        if (userMessage == null || userMessage.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "问题不能为空");
        }

        try {
            HttpResponse<String> response = sendChatRequest(buildPayload(systemPrompt, userMessage), Duration.ofSeconds(30));
            return parseAnswer(response.body());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw mapException(e);
        }
    }

    private HttpResponse<String> sendChatRequest(Map<String, Object> body, Duration timeout) {
        if (baseUrl.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "AI BaseUrl 未配置");
        }

        try {
            String payload = objectMapper.writeValueAsString(body == null ? Collections.emptyMap() : body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .timeout(timeout == null ? Duration.ofSeconds(30) : timeout)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw mapHttpError(response.statusCode(), response.body());
            }
            return response;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw mapException(e);
        }
    }

    private static BusinessException mapHttpError(int statusCode, String responseBody) {
        String body = responseBody == null ? "" : responseBody.trim();
        if (body.length() > 400) {
            body = body.substring(0, 400);
        }

        String msg;
        if (statusCode == 401 || statusCode == 403) {
            msg = "AI 鉴权失败（API Key 无效或无权限）";
        } else if (statusCode == 429) {
            msg = "AI 服务限流（HTTP 429）";
        } else {
            msg = "AI 请求失败（HTTP " + statusCode + "）";
        }
        if (!body.isBlank()) {
            msg = msg + "：" + body;
        }
        return new BusinessException(ErrorCode.INTERNAL_ERROR, msg);
    }

    private static BusinessException mapException(Exception e) {
        Throwable t = e;
        while (t.getCause() != null && t != t.getCause()) {
            t = t.getCause();
        }

        if (t instanceof UnknownHostException) {
            return new BusinessException(ErrorCode.INTERNAL_ERROR, "无法解析 AI 服务域名，请检查网络/DNS");
        }
        if (t instanceof ConnectException) {
            return new BusinessException(ErrorCode.INTERNAL_ERROR, "无法连接到 AI 服务，请检查网络或代理设置");
        }
        if (t instanceof HttpTimeoutException) {
            return new BusinessException(ErrorCode.INTERNAL_ERROR, "AI 请求超时，请稍后重试");
        }
        if (t instanceof SSLHandshakeException) {
            return new BusinessException(ErrorCode.INTERNAL_ERROR, "AI TLS 握手失败，请检查系统时间或证书链");
        }
        if (t instanceof IllegalArgumentException) {
            return new BusinessException(ErrorCode.VALIDATION_ERROR, "AI BaseUrl 格式不正确");
        }
        return new BusinessException(ErrorCode.INTERNAL_ERROR, "AI 请求失败");
    }

    private void parseAnswerUnchecked(String responseBody) {
        try {
            parseAnswer(responseBody);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "AI 响应解析失败");
        }
    }

    /**
     * 构造 OpenAI Compatible 的请求体。
     */
    private Map<String, Object> buildPayload(String systemPrompt, String userMessage) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt == null ? "" : systemPrompt),
                Map.of("role", "user", "content", userMessage)
        ));
        body.put("temperature", 0.2);
        return body;
    }

    /**
     * 从响应 JSON 中提取 assistant 的回答文本。
     */
    private String parseAnswer(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody == null ? "" : responseBody);
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (content.isMissingNode() || content.isNull()) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "AI response malformed");
        }
        String text = content.asText("");
        return text == null ? "" : text.trim();
    }

    /**
     * 去掉 baseUrl 末尾的斜杠，避免拼接路径出现双斜杠。
     */
    private static String stripTrailingSlash(String url) {
        if (url == null) {
            return "";
        }
        String u = url.trim();
        while (u.endsWith("/")) {
            u = u.substring(0, u.length() - 1);
        }
        return u;
    }
}
