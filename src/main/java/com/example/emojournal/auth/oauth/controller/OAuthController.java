package com.example.emojournal.auth.oauth.controller;

import com.example.emojournal.auth.oauth.dto.AuthorizationCodeRequest;
import com.example.emojournal.auth.oauth.dto.OAuthLoginTokenDto;
import com.example.emojournal.auth.oauth.service.OAuthLoginFacadeService;
import com.example.emojournal.auth.oauth.utils.GoogleLoginParams;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

/**
 * OAuth 2.0 인증 컨트롤러
 *
 * 이 컨트롤러는 OAuth 2.0 Authorization Code Grant 플로우를 통한
 * Google 소셜 로그인 기능을 제공합니다.
 *
 * 주요 기능:
 * - Google OAuth 인증 코드 처리
 * - PKCE (Proof Key for Code Exchange) 보안 검증
 * - Access Token 및 Refresh Token 발급
 * - 보안 쿠키를 통한 Refresh Token 저장
 *
 * OAuth 플로우:
 * 1. 프론트엔드에서 Google 인증 후 Authorization Code 전달
 * 2. Authorization Code를 Access Token으로 교환
 * 3. Google 사용자 정보 조회 및 회원 가입/로그인 처리
 * 4. JWT Access Token 발급 및 Refresh Token 쿠키 설정
 *
 * @author EmoJournal Team
 * @version 1.0
 * @see OAuthLoginFacadeService
 * @see GoogleLoginParams
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/login/oauth2/code")
public class OAuthController {

    /** OAuth 로그인 파사드 서비스 - 전체 OAuth 플로우 처리 */
    private final OAuthLoginFacadeService oAuthLoginFacadeService;

    /**
     * Google OAuth 2.0 로그인 처리
     *
     * Google OAuth 인증 완료 후 전달받은 Authorization Code를 사용하여
     * 사용자 인증 및 토큰 발급을 처리합니다.
     *
     * PKCE (Proof Key for Code Exchange) 보안 메커니즘을 사용하여
     * Authorization Code Interception Attack을 방지합니다.
     *
     * @param request HTTP 요청 객체 (클라이언트 정보 추출용)
     * @param authorizationCodeRequest OAuth 인증 요청 정보
     *        - code: Google에서 발급한 Authorization Code
     *        - redirectUri: 인증 완료 후 리다이렉트 URI
     *        - codeVerifier: PKCE code_verifier 값
     * @return ResponseEntity 응답 객체
     *         - Cookie: HttpOnly Refresh Token (7일간 유효)
     *         - Body: Access Token, 토큰 타입, 만료 시간
     * @throws Exception OAuth 처리 중 발생하는 예외
     *
     * @apiNote
     * - Refresh Token은 보안을 위해 HttpOnly 쿠키로 저장됩니다
     * - Access Token은 응답 바디로 전달되어 프론트엔드에서 관리됩니다
     * - CORS 및 CSRF 공격 방어를 위한 SameSite=Lax 설정 적용
     */
    @PostMapping("/google")
    public ResponseEntity<?> loginGoogle(
            HttpServletRequest request,
            @RequestBody AuthorizationCodeRequest authorizationCodeRequest
    ) throws Exception {

        /*
         * STEP 1: Google OAuth 파라미터 설정
         * Authorization Code Grant 플로우에 필요한 파라미터들을 설정합니다.
         */
        GoogleLoginParams params = new GoogleLoginParams();
        params.setAuthorizationCode(authorizationCodeRequest.getCode());        // Google에서 발급한 인증 코드
        params.setRedirectUri(authorizationCodeRequest.getRedirectUri());       // 인증 완료 후 리다이렉트 URI
        params.setCodeVerifier(authorizationCodeRequest.getCodeVerifier());     // PKCE 보안 검증을 위한 코드

        log.info("Google OAuth 로그인 요청 - Authorization Code: {}, Redirect URI: {}",
                authorizationCodeRequest.getCode(), authorizationCodeRequest.getRedirectUri());

        /*
         * STEP 2: OAuth 로그인 처리
         * 1) Authorization Code를 Access Token으로 교환
         * 2) Google 사용자 정보 조회
         * 3) 회원 가입 또는 기존 회원 로그인
         * 4) JWT Access Token 및 Refresh Token 발급
         */
        OAuthLoginTokenDto oAuthLoginTokenDto = oAuthLoginFacadeService.handleOAuthLogin(request, params);

        /*
         * STEP 3: Refresh Token 보안 쿠키 설정
         * 보안을 위해 Refresh Token은 HttpOnly 쿠키로 저장하여
         * XSS 공격으로부터 보호합니다.
         */
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", oAuthLoginTokenDto.getRefreshToken())
                .httpOnly(true)                    // JavaScript 접근 차단으로 XSS 공격 방지
                .secure(true)                      // HTTPS 환경에서만 전송 (운영 환경 보안)
                .path("/auth")                     // 특정 경로에서만 쿠키 전송으로 노출 최소화
                .maxAge(Duration.ofDays(7))        // 7일간 유효
                .sameSite("Lax")                   // CSRF 공격 방어 및 크로스 사이트 요청 제한
                .build();

        /*
         * STEP 4: 로그인 성공 응답
         * Access Token은 응답 바디로 전달하여 프론트엔드에서
         * Authorization 헤더로 API 요청 시 사용할 수 있도록 합니다.
         */
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(Map.of(
                        "accessToken", oAuthLoginTokenDto.getAuthTokens().getAccessToken(),    // JWT Access Token
                        "tokenType", oAuthLoginTokenDto.getAuthTokens().getGrantType(),        // Bearer 토큰 타입
                        "expiresIn", oAuthLoginTokenDto.getAuthTokens().getExpiresIn()         // 토큰 만료 시간 (초)
                ));
    }

}
