package com.example.emojournal.auth.oauth.client;

import com.example.emojournal.auth.oauth.dto.response.GoogleAccessTokenResponse;
import com.example.emojournal.auth.oauth.dto.response.GoogleInfoResponse;
import com.example.emojournal.auth.oauth.dto.response.GoogleTokenResponse;
import com.example.emojournal.auth.oauth.utils.OAuthInfoResponse;
import com.example.emojournal.auth.oauth.utils.OAuthLoginParams;
import com.example.emojournal.auth.jwt.entity.item.OAuthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Google OAuth 2.0 API 클라이언트
 *
 * Google OAuth 서비스와의 통신을 담당하는 클라이언트 클래스입니다.
 * Google Calendar 권한을 포함한 OAuth 인증 플로우와 사용자 정보 조회,
 * 액세스 토큰 갱신 기능을 제공합니다.
 *
 * 주요 기능:
 * - 인증 코드를 이용한 액세스 토큰 요청
 * - 액세스 토큰을 통한 사용자 정보 조회
 * - 리프레시 토큰을 이용한 액세스 토큰 갱신
 * - Google Calendar API 접근 권한 포함
 *
 * OAuth 2.0 플로우:
 * 1. 클라이언트가 Google 로그인 페이지로 리다이렉트
 * 2. 사용자 동의 후 인증 코드 발급
 * 3. 인증 코드로 액세스 토큰 및 리프레시 토큰 요청
 * 4. 액세스 토큰으로 사용자 정보 조회
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class GoogleApiClient implements OAuthApiClient{

    /** OAuth 2.0 인증 코드 그랜트 타입 */
    private static final String GRANT_TYPE = "authorization_code";

    /** Google OAuth 클라이언트 ID */
    @Value("${oauth2.google.client-id}")
    private String GOOGLE_CLIENT_ID;

    /** Google OAuth 클라이언트 시크릿 */
    @Value("${oauth2.google.client-secret}")
    private String GOOGLE_CLIENT_SECRET;

    /** Google 로그인 완료 후 리다이렉트될 URL */
    @Value("${oauth2.google.redirect-uri}")
    private String LOGIN_REDIRECT_URL;

    /** Google OAuth Authorization Server URL */
    @Value("${oauth2.google.authorization-uri}")
    private String AUTHORIZATION_URL;

    /** HTTP 요청을 위한 RestTemplate */
    private final RestTemplate restTemplate;

    /**
     * 현재 OAuth 제공자 타입을 반환합니다.
     *
     * @return Google OAuth 제공자 열거형
     */
    @Override
    public OAuthProvider oAuthProvider() {
        return OAuthProvider.GOOGLE;
    }

    /**
     * Google OAuth 서버로부터 액세스 토큰을 요청합니다.
     *
     * 클라이언트에서 받은 인증 코드를 Google OAuth 서버에 전송하여
     * 액세스 토큰과 리프레시 토큰을 발급받습니다.
     * Google Calendar API 접근 권한도 함께 요청합니다.
     *
     * @param params OAuth 로그인 파라미터 (인증 코드 포함)
     * @return Google에서 발급한 토큰 응답 (액세스 토큰, 리프레시 토큰 등)
     * @throws IllegalStateException 토큰 요청 실패 시
     */
    @Override
    public GoogleTokenResponse requestAccessToken(OAuthLoginParams params) {
        String url = "https://oauth2.googleapis.com/token";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = params.makeBody();
        body.add("client_id", GOOGLE_CLIENT_ID);
        body.add("client_secret", GOOGLE_CLIENT_SECRET);
        body.add("redirect_uri", LOGIN_REDIRECT_URL); // OAuth 플로우에 필수 파라미터
        body.add("scope", "openid email profile https://www.googleapis.com/auth/calendar"); // 캘린더 권한 추가

        // 요청 파라미터 로깅
        for (String key : body.keySet()) {
            log.info(key + " : " +  body.get(key).toString());
        }

        HttpEntity<?> request = new HttpEntity<>(body, httpHeaders);

        GoogleTokenResponse response = restTemplate.postForObject(url, request, GoogleTokenResponse.class);

        log.info(response.toString());

        if (response == null) {
            throw new IllegalStateException("Google 토큰 요청 응답이 null입니다.");
        }

        return response;
    }


    /**
     * Google 액세스 토큰을 사용하여 사용자 정보를 조회합니다.
     *
     * Google OAuth 2.0 UserInfo API를 호출하여 인증된 사용자의
     * 기본 프로필 정보(이메일, 이름 등)를 가져옵니다.
     *
     * @param accessToken Google에서 발급받은 유효한 액세스 토큰
     * @return 사용자의 OAuth 정보 (이메일, 닉네임, 프로필 등)
     * @throws RuntimeException API 호출 실패 또는 토큰 무효 시
     */
    @Override
    public OAuthInfoResponse requestOauthInfo(String accessToken) {
        String url = "https://www.googleapis.com/oauth2/v2/userinfo";

        log.info("Google 사용자 정보 요청 시작");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.set("Authorization","Bearer " + accessToken);

        MultiValueMap<String,String> body = new LinkedMultiValueMap<>();

        HttpEntity<?> request = new HttpEntity<>(body, httpHeaders);

        return restTemplate.exchange(
                url, HttpMethod.GET,request, GoogleInfoResponse.class
        ).getBody();

    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.
     *
     * 기존 액세스 토큰이 만료되었을 때 리프레시 토큰을 사용하여
     * 사용자 재인증 없이 새로운 액세스 토큰을 갱신합니다.
     *
     * @param refreshToken Google에서 발급받은 유효한 리프레시 토큰
     * @return 새로 발급받은 액세스 토큰 정보
     * @throws RuntimeException 토큰 갱신 실패 시
     */
    public GoogleAccessTokenResponse refreshAccessToken(String refreshToken) {
        String url = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", GOOGLE_CLIENT_ID);
        params.add("client_secret",GOOGLE_CLIENT_SECRET);
        params.add("refresh_token", refreshToken);
        params.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<GoogleAccessTokenResponse> response = restTemplate.exchange(url, HttpMethod.POST, request, GoogleAccessTokenResponse.class);

        return response.getBody();

    }
}
