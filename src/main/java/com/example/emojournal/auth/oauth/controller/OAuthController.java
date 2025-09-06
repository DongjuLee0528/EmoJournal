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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/login/oauth2/code")
public class OAuthController {

    private final OAuthLoginFacadeService oAuthLoginFacadeService;

    @PostMapping("/google")
    public ResponseEntity<?> loginGoogle(
            HttpServletRequest request,
            @RequestBody AuthorizationCodeRequest authorizationCodeRequest
    ) throws Exception {

        // ✅ Authorization Code + PKCE + Redirect URI 세팅
        GoogleLoginParams params = new GoogleLoginParams();
        params.setAuthorizationCode(authorizationCodeRequest.getCode());
        params.setRedirectUri(authorizationCodeRequest.getRedirectUri());
        params.setCodeVerifier(authorizationCodeRequest.getCodeVerifier());

        log.info("authorizationCodeRequest : " + authorizationCodeRequest.toString());

        // ✅ 로그인 처리 (토큰 교환 + 유저 생성/조회 + JWT 발급)
        OAuthLoginTokenDto oAuthLoginTokenDto = oAuthLoginFacadeService.handleOAuthLogin(request, params);

        // ✅ Refresh Token을 HttpOnly Cookie로 저장
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", oAuthLoginTokenDto.getRefreshToken())
                .httpOnly(true)        // JS 접근 차단
                .secure(true)          // 운영(HTTPS) 환경에서는 true
                .path("/auth")         // /auth 경로에서만 전송
                .maxAge(Duration.ofDays(7))
                .sameSite("Lax")       // CSRF 방어
                .build();

        // ✅ Access Token 및 관련 정보 Body로 응답
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(Map.of(
                        "accessToken", oAuthLoginTokenDto.getAuthTokens().getAccessToken(),
                        "tokenType", oAuthLoginTokenDto.getAuthTokens().getGrantType(),
                        "expiresIn", oAuthLoginTokenDto.getAuthTokens().getExpiresIn()
                ));
    }

}
