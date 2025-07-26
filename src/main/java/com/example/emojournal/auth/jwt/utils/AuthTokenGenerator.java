package com.example.emojournal.auth.jwt.utils;

import com.example.emojournal.auth.jwt.dto.AuthTokens;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;


// 스프링에서 jwt 토큰 생성하는 클래스
@Component
@RequiredArgsConstructor
public class AuthTokenGenerator {
    private static final String BEARER_TYPE = "Bearer";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30;            // 30분
//    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7일

    private final JwtTokenProvider jwtTokenProvider;

    public AuthTokens generate (Long memberId) {
        long now = (new Date()).getTime();
        Date accessTokenExpiredAt = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
//        Date refreshTokenExpiredAt  = new Date(now + REFRESH_TOKEN_EXPIRE_TIME);

        String subject = memberId.toString();
        String accessToken = jwtTokenProvider.generate(subject, accessTokenExpiredAt);
//        String refreshToken = jwtTokenProvider.generate(subject, refreshTokenExpiredAt);

        // 만료시간에 1000L 을 나누는이유 밀리 세컨드 단위에서 초 단위로 변환 하려고
        // 변환이유는 Oauth 표준이 초 단위 인가봄
//        return AuthTokens.of(accessToken,refreshToken,BEARER_TYPE,ACCESS_TOKEN_EXPIRE_TIME / 1000L);
        return AuthTokens.of(accessToken,BEARER_TYPE,ACCESS_TOKEN_EXPIRE_TIME / 1000L);

    }

}
