package com.example.emojournal.auth.jwt.interceptor;

import com.example.emojournal.auth.jwt.utils.AuthenticationContextHolder;
import com.example.emojournal.auth.jwt.utils.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@RequiredArgsConstructor
@Component
    public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 멤버 아이디가 있다면
        if(AuthenticationContextHolder.getContext() != null) {
            return true;
        } else {
            log.warn("Invalid or missing access token");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access token is missing or invalid");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        AuthenticationContextHolder.clear();
        log.debug("AuthenticationContextHolder cleared");
    }

}
