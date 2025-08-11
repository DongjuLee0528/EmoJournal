package com.example.emojournal.auth.jwt.filter;

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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            return;
        }

        String requestTokenHeader = request.getHeader("Authorization");
        String requestURI = request.getRequestURI();

        Long memberId = null;
        String jwtToken = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                memberId = jwtTokenProvider.extractMemberId(jwtToken);
                log.debug("JWT 토큰에서 회원 ID 추출: {}", memberId);
            } catch (Exception e) {
                log.warn("JWT 토큰 파싱 실패: {}", e.getMessage());
            }
        } else {
            log.debug("JWT 토큰이 Bearer로 시작하지 않음. URI: {}", requestURI);
        }

        if (memberId != null && jwtTokenProvider.validateToken(jwtToken)) {
            request.setAttribute("memberId", memberId);
            log.debug("JWT 토큰 검증 성공, 요청에 memberId 설정 완료");
        } else if (extractRefreshTokenFromCookie(request) != null) {
            log.debug("RefreshToken 존재 -> 필터 통과");
            filterChain.doFilter(request, response);
            return;
        } else {
            log.warn("유효하지 않은 토큰이거나 토큰이 없음");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.getWriter().write("{\"error\": \"Unauthorized - Invalid or missing token\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/login/oauth2/") ||
                path.startsWith("/api/login/oauth2/") ||
                path.startsWith("/auth/") ||
                path.equals("/api/emotion/health") ||
                path.equals("/api/diary/health") ||
                path.startsWith("/images/") ||
                path.startsWith("/uploads/") ||
                path.startsWith("/api/emotion/categories") ||
                path.equals("/favicon.ico") ||
                path.startsWith("/login/oauth2/code");
    }
}
