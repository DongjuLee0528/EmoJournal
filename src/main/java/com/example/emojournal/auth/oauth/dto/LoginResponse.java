package com.example.emojournal.auth.oauth.dto;

import com.example.emojournal.auth.jwt.dto.AuthTokens;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private Long memberId;
    private AuthTokens authTokens;
    private OAuthTokens oAuthTokens;
}