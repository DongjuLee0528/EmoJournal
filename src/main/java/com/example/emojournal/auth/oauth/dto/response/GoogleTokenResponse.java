package com.example.emojournal.auth.oauth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import lombok.Getter;
import lombok.NoArgsConstructor;

@ToString
@Getter
@NoArgsConstructor
public class GoogleTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("id_token")
    private String idToken;

    @JsonProperty("refresh_token")
    private String refreshToken; // 있을 수도 있고 없을 수도 있음
}

