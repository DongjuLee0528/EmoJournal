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

/**
 * JWT 인증 필터 클래스
 *
 * HTTP 요청마다 JWT 토큰을 검증하여 사용자 인증을 처리하는 서블릿 필터입니다.
 * Spring Security의 OncePerRequestFilter를 상속하여 요청당 한 번만 실행되도록 보장합니다.
 *
 * 주요 기능:
 * - Authorization 헤더에서 Bearer 토큰 추출
 * - JWT 토큰 유효성 검증 (서명, 만료시간)
 * - 토큰에서 회원 ID 추출하여 인증 컨텍스트에 저장
 * - OPTIONS 요청에 대한 CORS 처리
 * - 인증 실패 시 적절한 로깅 및 처리
 *
 * 처리 흐름:
 * 1. OPTIONS 요청은 즉시 통과
 * 2. Authorization 헤더에서 Bearer 토큰 추출
 * 3. 토큰 유효성 검증
 * 4. 유효한 경우 회원 ID를 AuthenticationContextHolder에 저장
 * 5. 다음 필터로 요청 전달
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** JWT 토큰 생성 및 검증을 담당하는 프로바이더 */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * HTTP 요청마다 실행되는 JWT 인증 필터의 핵심 메서드
     *
     * 각 HTTP 요청에 대해 JWT 토큰을 검증하고 인증 정보를 설정합니다.
     * OPTIONS 요청은 CORS preflight 요청으로 간주하여 바로 통과시킵니다.
     *
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 처리 중 오류 발생 시
     * @throws IOException 입출력 오류 발생 시
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.debug("[JWT_FILTER] doFilterInternal 시작");

        String uri = request.getRequestURI();
        String method = request.getMethod();
        log.debug("[JWT_FILTER] 요청 정보 - URI: {}, Method: {}", uri, method);

        // CORS preflight 요청 처리
        if("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            log.debug("[JWT_FILTER] OPTIONS 요청 바로 통과 - URI: {}", uri);
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        try{
            String token = resolveToken(request);
            log.debug("[JWT_FILTER] 토큰 추출 결과: {}", token != null ? "토큰 있음" : "토큰 없음");

            // 토큰이 존재하고 유효한 경우 인증 정보 설정
            if(token != null && jwtTokenProvider.validateToken(token)) {
                Long memberId = jwtTokenProvider.extractMemberId(token);
                log.info("[JWT_FILTER] 토큰 검증 성공 - memberId: {}, URI: {}", memberId, uri);

                // request attribute와 인증 컨텍스트에 회원 ID 설정
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
            // 요청 처리 완료 후 인증 컨텍스트 정리
            AuthenticationContextHolder.clear();
        }
    }

    /**
     * HTTP 요청에서 JWT 토큰을 추출합니다.
     *
     * Authorization 헤더에서 "Bearer " 접두사를 제거하고 실제 JWT 토큰만 추출합니다.
     * 헤더가 없거나 Bearer 형식이 아닌 경우 null을 반환합니다.
     *
     * @param request HTTP 요청 객체
     * @return 추출된 JWT 토큰 문자열, 토큰이 없으면 null
     */
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        log.debug("[JWT_FILTER] Authorization 헤더: {}", bearer != null ? "Bearer 토큰 있음" : "헤더 없음");

        if(bearer != null && bearer.startsWith("Bearer ")) {
            String token = bearer.substring(7); // "Bearer " 접두사 제거
            log.debug("[JWT_FILTER] 토큰 추출 성공 - 길이: {}", token.length());
            return token;
        }
        return null;
    }
}
