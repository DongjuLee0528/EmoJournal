package com.example.emojournal.auth.oauth.dto;

import com.example.emojournal.auth.oauth.entity.GoogleToken;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleTokenDto {

    private Long id;
    private Long memberId;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime accessTokenExpiresAt;
    private boolean revoked;

    public static GoogleTokenDto from(GoogleToken token) {
        return GoogleTokenDto.builder()
                .id(token.getId())
                .memberId(token.getMember().getId())
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .accessTokenExpiresAt(token.getAccessTokenExpiresAt())
                .revoked(token.isRevoked())
                .build();
    }
}

