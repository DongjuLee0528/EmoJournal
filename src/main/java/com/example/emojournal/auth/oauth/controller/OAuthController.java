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

    private  final OAuthLoginFacadeService oAuthLoginFacadeService;

    // 컨트롤러의 책임 분리 너무 많은 일을 하고있음
    // 문제가 뭐냐면 컨트롤러를 서비스 하나로 묶어서 해야 되는데 그게 안됨
    // 그리고 트렌젝션 신경써야됨
    @PostMapping("/google")
    public ResponseEntity<?> loginGoogle(HttpServletRequest request,@RequestBody AuthorizationCodeRequest authorizationCodeRequest) throws Exception {

        // authorization code 를 넣어줌
        GoogleLoginParams params = new GoogleLoginParams();
        params.setAuthorizationCode(authorizationCodeRequest.getCode());

        OAuthLoginTokenDto oAuthLoginTokenDto = oAuthLoginFacadeService.handleOAuthLogin(request, params);

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", oAuthLoginTokenDto.getGoogleRefreshToken())
                .httpOnly(true) // 자바스크립트에서 접근불가
                .secure(false) // HTTPS 일때만 전송됨
                .path("/auth") // /auth 요청할 때만 쿠키 자동 포함됨
                .maxAge(Duration.ofDays(7)) // 쿠키 만료 시간 7일
                .sameSite("Lax") // 외부 사이트 요청 시 쿠키가 전송되지 않게 하는 코드
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,refreshTokenCookie.toString())
                .body(Map.of(
                        "accessToken",oAuthLoginTokenDto.getAccessToken(),
                        "tokenType", oAuthLoginTokenDto.getAuthTokens().getGrantType(),
                        "expiresIn",oAuthLoginTokenDto.getAuthTokens().getExpiresIn()
                ));
    }

}