package com.example.emojournal.auth.jwt.controller;

import com.example.emojournal.auth.jwt.dto.AuthTokens;
import com.example.emojournal.auth.jwt.service.RefreshTokenService;
import com.example.emojournal.auth.jwt.service.TokenReissueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
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

    // access token 이 만료되서 refresh token 이 있는지 확인하고 access token 을 재발급 하는 메서드
    @PostMapping("/auth/reissue")
    public ResponseEntity<?> reissueAccessToken(@CookieValue(name = "refreshToken") String refreshTokenCookie) {

        log.info("refreshToken Cookie : " + refreshTokenCookie);

        AuthTokens authTokens = tokenReissueService.reissueAccessToken(refreshTokenCookie).orElseThrow(NoSuchElementException::new);

        return ResponseEntity.ok()
                .body(Map.of(
                        "accessToken",authTokens.getAccessToken(),
                        "tokenType",authTokens.getGrantType(),
                        "expiresIn",authTokens.getExpiresIn()
                ));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(@CookieValue(name = "refreshToken") String refreshTokenCookie) {
        log.info("logout");

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