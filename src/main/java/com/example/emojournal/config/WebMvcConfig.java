package com.example.emojournal.config;

import com.example.emojournal.auth.jwt.interceptor.JwtAuthenticationInterceptor;
import com.example.emojournal.auth.oauth.interceptor.GoogleTokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final GoogleTokenInterceptor googleTokenInterceptor;
    private final JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/member", "/api/diary/**")
                .excludePathPatterns("/api/diary/health", "/uploads/**", "/images/**");

        registry.addInterceptor(googleTokenInterceptor)
                .addPathPatterns("/calendar");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(604800);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/")
                .setCachePeriod(604800);
    }
}
