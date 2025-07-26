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


        String uri = request.getRequestURI();

        if(isPublicPath(uri)) {
            filterChain.doFilter(request,response);
            return;
        }

        String token = resolveToken(request);

        log.info("token : " + token);

        // access token 이 있고 토큰이 정확하다면
        if(token != null && jwtTokenProvider.validateToken(token)) {
            Long memberId = jwtTokenProvider.extractMemberId(token);

            // 요청에 memberId 를 넣어놓음
            request.setAttribute("memberId",memberId);

            filterChain.doFilter(request,response);
        }
        // 리프레쉬 토큰이 있으면 다음 필터로 넘어감
        else if(extractRefreshTokenFromCookie(request) != null) {
            log.info("로그아웃");
            filterChain.doFilter(request,response);
        }
        else {
            log.warn("유효하지 않은 토큰이거나 토큰이 없음");

            // 401 상태 코드 설정
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");

            response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");


            // 응답 body에 메시지를 추가할 수도 있음 (선택)
            response.getWriter().write("{\"error\": \"Unauthorized - Invalid or missing token\"}");
            // 필터 체인 종료 (주의!)
            return;

        }

    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null) return null;

        for(Cookie cookie : cookies) {
            if("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if(bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }


    // 토큰 인증이 필요없는 요청 경로 작성하는 메서드
    public boolean isPublicPath(String uri) {
        return uri.startsWith("/login/oauth2/code");
    }


}
