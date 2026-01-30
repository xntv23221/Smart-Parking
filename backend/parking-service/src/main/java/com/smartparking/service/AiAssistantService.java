package com.smartparking.service;

/**
 * AI 助手能力抽象，面向 Controller 提供问答接口。
 */
public interface AiAssistantService {
    /**
     * 基于当前系统上下文（例如车位余量）回答用户问题。
     */
    String ask(String question);
}

