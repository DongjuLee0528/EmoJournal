package com.example.emojournal.auth.oauth.client;

import com.example.emojournal.auth.oauth.utils.OAuthInfoResponse;
import com.example.emojournal.auth.oauth.utils.OAuthLoginParams;
import com.example.emojournal.auth.jwt.entity.item.OAuthProvider;
import com.example.emojournal.auth.oauth.dto.response.GoogleTokenResponse;

public interface OAuthApiClient {
    // Client 의 타입 반환
    OAuthProvider oAuthProvider();
    // Authorization Code 를 기반으로 인증 API 를 요청해서 Access Token 을 획득
    GoogleTokenResponse requestAccessToken(OAuthLoginParams params);
    // Access Token 을 기반으로 Email, Nickname 이 포함된 프로필 정보를 획득
    OAuthInfoResponse requestOauthInfo(String accessToken);
}
