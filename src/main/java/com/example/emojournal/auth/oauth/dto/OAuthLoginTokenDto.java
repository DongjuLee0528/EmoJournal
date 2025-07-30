package com.example.emojournal.auth.oauth.dto;

import com.example.emojournal.auth.jwt.dto.AuthTokens;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthLoginTokenDto {
    private String googleRefreshToken;
    private String accessToken;
    private AuthTokens authTokens;
}
