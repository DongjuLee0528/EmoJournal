package com.example.emojournal.auth.jwt.filter;

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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.debug("[JWT_FILTER] doFilterInternal 시작");

        String uri = request.getRequestURI();
        String method = request.getMethod();
        log.debug("[JWT_FILTER] 요청 정보 - URI: {}, Method: {}", uri, method);


        if("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            log.debug("[JWT_FILTER] OPTIONS 요청 바로 통과 - URI: {}", uri);
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        try{
            String token = resolveToken(request);
            log.debug("[JWT_FILTER] 토큰 추출 결과: {}", token != null ? "토큰 있음" : "토큰 없음");

            // 토큰이 있거나 토큰이 만료일이 일치한다면
            if(token != null && jwtTokenProvider.validateToken(token)) {
                Long memberId = jwtTokenProvider.extractMemberId(token);
                log.info("[JWT_FILTER] 토큰 검증 성공 - memberId: {}, URI: {}", memberId, uri);

                // request attribute에 memberId 설정
                request.setAttribute("memberId", memberId);
                AuthenticationContextHolder.setContext(memberId);

                log.debug("[JWT_FILTER] request attribute 설정 완료 - memberId: {}", memberId);
            } else {
                log.warn("[JWT_FILTER] 토큰 검증 실패 또는 토큰 없음 - URI: {}", uri);
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("[JWT_FILTER] JWT 필터 처리 중 오류 - URI: {}, 오류: {}", uri, e.getMessage(), e);
            throw e;
        } finally {
            AuthenticationContextHolder.clear();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        log.debug("[JWT_FILTER] Authorization 헤더: {}", bearer != null ? "Bearer 토큰 있음" : "헤더 없음");

        if(bearer != null && bearer.startsWith("Bearer ")) {
            String token = bearer.substring(7);
            log.debug("[JWT_FILTER] 토큰 추출 성공 - 길이: {}", token.length());
            return token;
        }
        return null;
    }
}
