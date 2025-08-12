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
@RequestMapping("/api/login/oauth2/code")
public class OAuthController {

    private final OAuthLoginFacadeService oAuthLoginFacadeService;

    @PostMapping("/google")
    public ResponseEntity<?> loginGoogle(HttpServletRequest request, @RequestBody AuthorizationCodeRequest authorizationCodeRequest) throws Exception {
        GoogleLoginParams params = new GoogleLoginParams();
        params.setAuthorizationCode(authorizationCodeRequest.getCode());

        OAuthLoginTokenDto oAuthLoginTokenDto = oAuthLoginFacadeService.handleOAuthLogin(request, params);

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", oAuthLoginTokenDto.getGoogleRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/auth")
                .maxAge(Duration.ofDays(7))
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(Map.of(
                        "accessToken", oAuthLoginTokenDto.getAccessToken(),
                        "tokenType", oAuthLoginTokenDto.getAuthTokens().getGrantType(),
                        "expiresIn", oAuthLoginTokenDto.getAuthTokens().getExpiresIn()
                ));
    }
}
