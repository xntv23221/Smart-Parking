package com.smartparking.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartparking.api.util.ResponseWriter;
import com.smartparking.common.api.Result;
import com.smartparking.common.error.ErrorCode;
import com.smartparking.manager.security.JwtService;
import com.smartparking.common.security.UserContext;
import com.smartparking.common.security.UserContextHolder;
import com.smartparking.common.security.UserRole;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class ClientInterceptor implements HandlerInterceptor {
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public ClientInterceptor(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Optional<Claims> claims = jwtService.parse(extractToken(request));
        if (claims.isEmpty()) {
            ResponseWriter.writeJson(response, objectMapper, 401, Result.fail(ErrorCode.UNAUTHORIZED.getCode(), "UNAUTHORIZED"));
            return false;
        }
        try {
            long userId = ((Number) claims.get().get("userId")).longValue();
            String role = String.valueOf(claims.get().get("role"));
            UserRole userRole = UserRole.valueOf(role);
            if (userRole != UserRole.CLIENT) {
                ResponseWriter.writeJson(response, objectMapper, 403, Result.fail(ErrorCode.FORBIDDEN.getCode(), "FORBIDDEN"));
                return false;
            }
            UserContextHolder.set(new UserContext(userId, userRole));
        } catch (Exception ex) {
            ResponseWriter.writeJson(response, objectMapper, 401, Result.fail(ErrorCode.UNAUTHORIZED.getCode(), "UNAUTHORIZED"));
            return false;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clear();
    }

    private static String extractToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null) {
            return "";
        }
        if (auth.startsWith("Bearer ")) {
            return auth.substring("Bearer ".length()).trim();
        }
        return auth.trim();
    }
}
