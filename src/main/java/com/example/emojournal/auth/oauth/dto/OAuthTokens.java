package com.example.emojournal.auth.oauth.dto;

import lombok.*;


// 소셜 로그인에서 주는 토큰들을 담는 클래스
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class OAuthTokens {

    private String accessToken;

    private String refreshToken;

    private Long expiresIn;
}
