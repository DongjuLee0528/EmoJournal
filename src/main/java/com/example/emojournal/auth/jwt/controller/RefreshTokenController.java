package com.example.emojournal.auth.jwt.controller;

import com.example.emojournal.auth.jwt.dto.AuthTokens;
import com.example.emojournal.auth.jwt.service.RefreshTokenService;
import com.example.emojournal.auth.jwt.service.TokenReissueService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RefreshTokenController {

    private final TokenReissueService tokenReissueService;

    private final RefreshTokenService refreshTokenService;

    // JWT 토큰을 말하는 것이다
    // access token 이 만료되서 refresh token 이 있는지 확인하고 access token 을 재발급 하는 메서드
    @PostMapping("/auth/reissue")
    public ResponseEntity<?> reissueAccessToken(@CookieValue(name = "refreshToken") String refreshTokenCookie) {

        // 리프레쉬 토큰이 없다면
        if(refreshTokenCookie == null) {
            log.warn("RefreshTokenCookie is null");
            // 권한 없는 응답을 보내줌
            return ResponseEntity.status(401).body("Refresh token is missing");
        }

        log.info("refreshToken Cookie : " + refreshTokenCookie);

        AuthTokens authTokens = tokenReissueService.reissueAccessToken(refreshTokenCookie);

        // 이미 멤버와 IP 가 일치하는 refresh token 이 있다면  NULL 을 반환
        if(authTokens == null) {
            log.info("authTokens is Null");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("인가를 받을 권한이 없습니다.");
        }

        return ResponseEntity.ok()
                .body(Map.of(
                        "accessToken",authTokens.getAccessToken(),
                        "tokenType",authTokens.getGrantType(),
                        "expiresIn",authTokens.getExpiresIn()
                ));
    }

    // JWT 토큰 로그아웃 하는걸 말하는 거다
    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(@CookieValue(name = "refreshToken",required = false) String refreshTokenCookie) {
        log.info("logout");

        if(refreshTokenCookie == null)
            return ResponseEntity.status(401).body(
                    Map.of("error", "유효하지 않은 리프레쉬 토큰입니다.")
            );

        log.info("refresh Token : "  +refreshTokenCookie);

        refreshTokenService.logout(refreshTokenCookie);

        ResponseCookie refreshTokenHeader = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/auth")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,refreshTokenHeader.toString())
                .body(Map.of("message","로그아웃 완료"));

    }

}