package com.smartparking.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartparking.common.api.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

public final class ResponseWriter {
    private ResponseWriter() {
    }

    public static void writeJson(HttpServletResponse response, ObjectMapper objectMapper, int httpStatus, Result<?> body) {
        try {
            response.setStatus(httpStatus);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), body);
        } catch (Exception ignored) {
        }
    }
}

