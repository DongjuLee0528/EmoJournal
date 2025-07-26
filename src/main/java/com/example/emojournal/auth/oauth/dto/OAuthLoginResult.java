package com.example.emojournal.auth.oauth.dto;

import com.example.emojournal.auth.oauth.utils.OAuthInfoResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OAuthLoginResult {
    private final String accessToken;
    private final OAuthInfoResponse userInfo;
}
