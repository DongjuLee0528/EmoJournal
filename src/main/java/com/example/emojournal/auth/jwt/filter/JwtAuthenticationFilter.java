package com.example.emojournal.auth.jwt.filter;

import com.example.emojournal.auth.jwt.utils.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");
        final String requestURI = request.getRequestURI();

        Long memberId = null;
        String jwtToken = null;

        // Authorization 헤더에서 JWT 토큰 추출
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                // 기존 JwtTokenProvider 사용
                memberId = jwtTokenProvider.extractMemberId(jwtToken);
                log.debug("JWT 토큰에서 회원 ID 추출: {}", memberId);
            } catch (Exception e) {
                log.warn("JWT 토큰 파싱 실패: {}", e.getMessage());
            }
        } else {
            log.debug("JWT 토큰이 Bearer로 시작하지 않음. URI: {}", requestURI);
        }

        // JWT 토큰 검증 및 SecurityContext 설정
        if (memberId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtTokenProvider.validateToken(jwtToken)) {
                log.debug("JWT 토큰 검증 성공: 회원 ID {}", memberId);

                // 사용자 인증 정보 생성 (memberId를 String으로 변환)
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(memberId.toString(), null, new ArrayList<>());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // 요청에 회원 정보 추가 (Controller에서 사용 가능)
                request.setAttribute("memberId", memberId);
                request.setAttribute("userId", "member_" + memberId); // 일기 API 호환용

                log.debug("사용자 인증 완료: 회원 ID {}", memberId);
            } else {
                log.warn("JWT 토큰 검증 실패: 회원 ID {}", memberId);
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // 인증이 필요 없는 경로들
        return path.startsWith("/login/oauth2/") ||        // OAuth 로그인
                path.startsWith("/auth/") ||               // 토큰 재발급
                path.equals("/api/emotion/health") ||      // 헬스체크
                path.equals("/api/diary/health") ||        // 헬스체크
                path.startsWith("/images/") ||             // 정적 이미지
                path.startsWith("/uploads/") ||            // 업로드된 파일
                path.startsWith("/api/emotion/categories") || // 감정 카테고리 (공개)
                path.equals("/favicon.ico");               // 파비콘
    }
}