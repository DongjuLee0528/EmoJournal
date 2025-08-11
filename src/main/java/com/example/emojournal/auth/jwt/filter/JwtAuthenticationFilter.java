package com.example.emojournal.auth.jwt.filter;

import com.example.emojournal.auth.jwt.entity.exception.InvalidAccessTokenException;
import com.example.emojournal.auth.jwt.utils.AuthenticationContextHolder;
import com.example.emojournal.auth.jwt.utils.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


// header 검사 , JWT 토큰 검증, 사용자 인증 객체 세팅
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("doFilterInternal");

        if("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            // ✅ CORS 헤더 직접 추가
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            return;
        }

        try{

            String token = resolveToken(request);
            log.info("token : " + token);

            // 토큰이 있거나 토큰이 만료일이 일치한다면
            if(token != null && jwtTokenProvider.validateToken(token)) {
                log.info("토큰이 있거나 토큰 만료일이 일치한다면");
                Long memberId = jwtTokenProvider.extractMemberId(token);

                AuthenticationContextHolder.setContext(memberId);
            }

            filterChain.doFilter(request, response);
        } finally {
            AuthenticationContextHolder.clear();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if(bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

}
