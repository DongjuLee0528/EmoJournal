package com.example.emojournal.auth.client;

import com.example.emojournal.auth.dto.GoogleInfoResponse;
import com.example.emojournal.auth.dto.OAuthInfoResponse;
import com.example.emojournal.auth.dto.OAuthLoginParams;
import com.example.emojournal.domain.item.OAuthProvider;
import com.example.emojournal.domain.token.GoogleTokens;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


@Slf4j
@RequiredArgsConstructor
@Component
public class GoogleApiClient implements OAuthApiClient{

    private static final String GRANT_TYPE = "authorization_code";

    @Value("${oauth2.google.client-id}")
    private String GOOGLE_CLIENT_ID;
    @Value("${oauth2.google.client-secret}")
    private String GOOGLE_CLIENT_SECRET;

    // 사용자가 구글 로그인 하면 리다이렉트 되는 url
    @Value("${oauth2.google.redirect-uri}")
    private String LOGIN_REDIRECT_URL;

    // Authorization code 를 Resource 서버에 보내줄때 쓰는 url
    @Value("${oauth2.google.authorization-uri}")
    private String AUTHORIZATION_URL;

    private final RestTemplate restTemplate;

    // 현재 구글의 Enum 타입을 반환해줌
    @Override
    public OAuthProvider oAuthProvider() {
        return OAuthProvider.GOOGLE;
    }

    // server 에서 Authorization server 에 access token 을 받기위해
    // 요청하는 과정
    @Override
    public String requestAccessToken(OAuthLoginParams params) {
        // ✅ 실제로 토큰을 요청할 URL
        String url = "https://oauth2.googleapis.com/token";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = params.makeBody();
        body.add("grant_type", GRANT_TYPE);
        body.add("client_id", GOOGLE_CLIENT_ID);
        body.add("client_secret", GOOGLE_CLIENT_SECRET);
        body.add("redirect_uri", LOGIN_REDIRECT_URL); // 여기서 이 redirect_uri는 꼭 필요함

        for (String key : body.keySet()) {
            log.info(key + " : " +  body.get(key).toString());
        }

        HttpEntity<?> request = new HttpEntity<>(body, httpHeaders);

        GoogleTokens response = restTemplate.postForObject(url, request, GoogleTokens.class);

        if (response == null) {
            throw new IllegalStateException("response가 null입니다.");
        }

        return response.getAccessToken();
    }


    @Override
    public OAuthInfoResponse requestOauthInfo(String accessToken) {
        String url = "https://www.googleapis.com/oauth2/v2/userinfo";

        log.info("requestOauthInfo 메서드");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.set("Authorization","Bearer " + accessToken);

        MultiValueMap<String,String> body = new LinkedMultiValueMap<>();

        HttpEntity<?> request = new HttpEntity<>(body, httpHeaders);

        return restTemplate.exchange(
                url, HttpMethod.GET,request, GoogleInfoResponse.class
        ).getBody();

    }
}
