package com.example.emojournal.auth.oauth.client;

import com.example.emojournal.auth.oauth.utils.OAuthInfoResponse;
import com.example.emojournal.auth.oauth.utils.OAuthLoginParams;
import com.example.emojournal.auth.oauth.dto.response.OAuthLoginResponse;
import com.example.emojournal.auth.oauth.dto.OAuthTokens;
import com.example.emojournal.auth.jwt.entity.item.OAuthProvider;
import com.example.emojournal.auth.oauth.dto.response.GoogleTokenResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// RequestOAuthInfoService
// OAuth 의 정보를 요청하는 service
@Component
public class RequestOAuthInfoService {

    // 제공자에 맞는 api 클라이언트 저장하는 필드
    private final Map<OAuthProvider, OAuthApiClient> clients;

    // 스프링이 OAuthApiClient 를 구현한 모든 빈을 찾아서 자동으로 주입합니다.
    public RequestOAuthInfoService(List<OAuthApiClient> clients) {
        this.clients = clients.stream().collect(
                Collectors.toUnmodifiableMap(OAuthApiClient::oAuthProvider, Function.identity())
        );
    }

    // 요청하는 메서드
    // 반환 받는 값은 OAuthInfoResponse
    // 매게변수는 OAuthLoginParams
    // OAuthLoginParams 는 인터페이스이다
    // 다형성을 이용해서 OAuthLoginParams 를 구현 한 클래스들이 담긴다.
    // 하지만 소셜로그인을 구글 로그인만 구현 했으므로 지금은 구글 로그인 하나만 구현한
    // 클래스가 매게변수에 담긴다. GoogleLoginParams 이 클래스다
    public OAuthLoginResponse request(OAuthLoginParams params) {

        // clients 는 Map 타입이고 키값으로 OAuthProvider 를 갖는다
        // OAuthProvider 는 Enum 타입이고 지금 값은 한가지 GOOGLE 이란 값을 갖는다.
        // 키 값에 GOOGLE enum 값을 넣고 value 로 반환하는 것은
        // GoogleApiClient 이다.
        OAuthApiClient client = clients.get(params.oAuthProvider());

        // 구글 서버에 access token 을 받기 위해 요청
        GoogleTokenResponse token = client.requestAccessToken(params);

        // 소셜로그인에서 준 access token,refresh token
        String oAuthRefreshToken = token.getRefreshToken();
        String oAuthAccessToken = token.getAccessToken();
        Long expiresIn = token.getExpiresIn();

        OAuthTokens oAuthTokens = new OAuthTokens(oAuthAccessToken, oAuthRefreshToken,expiresIn);

        // accessToken -> 사용자 정보
        OAuthInfoResponse oAuthInfoResponse = client.requestOauthInfo(token.getAccessToken());

        return new OAuthLoginResponse(oAuthInfoResponse,oAuthTokens);
    }
}