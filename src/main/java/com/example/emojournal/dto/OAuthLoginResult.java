package com.example.emojournal.dto;

import com.example.emojournal.auth.dto.OAuthInfoResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OAuthLoginResult {
    private final String accessToken;
    private final OAuthInfoResponse userInfo;
}
