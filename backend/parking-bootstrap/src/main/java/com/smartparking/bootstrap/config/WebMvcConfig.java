package com.smartparking.bootstrap.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartparking.api.security.AdminInterceptor;
import com.smartparking.api.security.ClientInterceptor;
import com.smartparking.api.security.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {
    private final AdminInterceptor adminInterceptor;
    private final ClientInterceptor clientInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;
    private final ObjectMapper objectMapper;

    public WebMvcConfig(AdminInterceptor adminInterceptor,
                        ClientInterceptor clientInterceptor,
                        RateLimitInterceptor rateLimitInterceptor,
                        ObjectMapper objectMapper) {
        this.adminInterceptor = adminInterceptor;
        this.clientInterceptor = clientInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
        this.objectMapper = objectMapper;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(clientInterceptor)
                .addPathPatterns("/api/client/**")
                .excludePathPatterns("/api/client/v1/map/**");
        registry.addInterceptor(adminInterceptor).addPathPatterns("/api/admin/**");
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        converter.setDefaultCharset(StandardCharsets.UTF_8);
        converters.add(converter);
    }
}
