package com.example.emojournal.config;

import com.example.emojournal.auth.jwt.interceptor.JwtAuthenticationInterceptor;
import com.example.emojournal.auth.oauth.interceptor.GoogleTokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final GoogleTokenInterceptor googleTokenInterceptor;
    private final JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/member");

        registry.addInterceptor(googleTokenInterceptor)
                .addPathPatterns("/calendar");
    }
}
