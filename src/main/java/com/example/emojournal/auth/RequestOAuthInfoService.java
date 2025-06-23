package com.example.emojournal.auth;

import com.example.emojournal.domain.item.OAuthProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RequestOAuthInfoService {

    // 제공자에 맞는 api 클라이언트 저장하는 필드
    private final Map<OAuthProvider,OAuthApiClient> clients;

    // 스프링이 OAuthApiClient 를 구현한 모든 빈을 찾아서 자동으로 주입합니다.
    public RequestOAuthInfoService(List<OAuthApiClient> clients) {
        this.clients = clients.stream().collect(
                Collectors.toUnmodifiableMap(OAuthApiClient::oAuthProvider, Function.identity())
        );
    }

    public OAuthInfoResponse request(OAuthLoginParams params) {
        // 어떤 클라이언트 쓸지 결정
        OAuthApiClient client = clients.get(params.oAuthProvider());
        // Authorization code -> access Token
        String accessToken = client.requestAccessToken(params);
        // accessToken -> 사용자 정보
        return client.requestOauthInfo(accessToken);
    }

}