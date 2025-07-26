package com.example.emojournal.auth.oauth.controller;

import com.example.emojournal.auth.oauth.dto.LoginResponse;
import com.example.emojournal.auth.jwt.dto.AuthTokens;
import com.example.emojournal.auth.oauth.utils.GoogleLoginParams;
import com.example.emojournal.auth.oauth.dto.OAuthTokens;
import com.example.emojournal.auth.jwt.utils.crypto.CryptoUtil;
import com.example.emojournal.member.entity.Member;
import com.example.emojournal.auth.jwt.entity.RefreshToken;
import com.example.emojournal.auth.oauth.dto.AuthorizationCodeRequest;
import com.example.emojournal.auth.oauth.dto.GoogleTokenDto;
import com.example.emojournal.auth.oauth.service.GoogleTokenService;
import com.example.emojournal.member.service.MemberService;
import com.example.emojournal.auth.oauth.service.OAuthLoginService;
import com.example.emojournal.auth.jwt.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/login/oauth2/code")
public class OAuthController {

    private final OAuthLoginService oAuthLoginService;

    private final RefreshTokenService refreshTokenService;

    private final MemberService memberService;

    private final GoogleTokenService googleTokenService;

    private final CryptoUtil cryptoUtil;

    @PostMapping("/google")
    public ResponseEntity<?> loginGoogle(@RequestBody AuthorizationCodeRequest codeRequest) throws Exception {

        // authorization code 를 넣어줌
        GoogleLoginParams params = new GoogleLoginParams();
        params.setAuthorizationCode(codeRequest.getCode());

        // 받은 code 를 매게변수로 넘겨주고
        // 토큰 객체, memberId 를 받아옴
        LoginResponse loginResponse = oAuthLoginService.login(params);

        // 스프링에서 만든 토큰들
        AuthTokens authTokens = loginResponse.getAuthTokens();

        // 소셜로그인 서버에서 만든 토큰들
        OAuthTokens oAuthTokens = loginResponse.getOAuthTokens();

        log.info("소셜로그인 : " + oAuthTokens.toString());

        // 구글 서버에서 준 access token
        String googleAccessToken = oAuthTokens.getAccessToken();

        // 구글 서버에서 준 refresh token
        String googleRefreshToken = oAuthTokens.getRefreshToken();

        // 스프링에서 만든 access token
        String accessToken = authTokens.getAccessToken();

        // 스프링 서버에서 만드는 refresh token
        String refreshToken = UUID.randomUUID().toString();

        // memberId 로 member 를찾아야됨
        Member member = memberService.findMemberById(loginResponse.getMemberId());

        // 구글 토큰 생성자
        GoogleTokenDto googleTokenDto = GoogleTokenDto.builder()
                .accessToken(cryptoUtil.encrypt(googleAccessToken))
                .refreshToken(cryptoUtil.encrypt(googleRefreshToken))
                .accessTokenExpiresAt(LocalDateTime.now().plusHours(1))
                .memberId(member.getId())
                .build();

        log.info("googleTokenInfo :  " + googleTokenDto);

        // 구글 토큰 저장
        googleTokenService.save(member.getId(),googleTokenDto);

        // 스프링 서버에서 만든 토큰 저장
        refreshTokenService.saveRefreshToken(RefreshToken.create(refreshToken, LocalDateTime.now().plusWeeks(1),member));

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true) // 자바스크립트에서 접근불가
                .secure(false) // HTTPS 일때만 전송됨
                .path("/auth") // /auth 요청할 때만 쿠키 자동 포함됨
                .maxAge(Duration.ofDays(7)) // 쿠키 만료 시간 7일
                .sameSite("Lax") // 외부 사이트 요청 시 쿠키가 전송되지 않게 하는 코드
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,refreshTokenCookie.toString())
                .body(Map.of(
                        "accessToken",accessToken,
                        "tokenType", authTokens.getGrantType(),
                        "expiresIn",authTokens.getExpiresIn()
                ));
    }

}