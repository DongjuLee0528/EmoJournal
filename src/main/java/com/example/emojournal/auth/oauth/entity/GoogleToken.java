package com.example.emojournal.auth.oauth.entity;

import com.example.emojournal.auth.oauth.dto.GoogleTokenDto;
import com.example.emojournal.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "google_token")
public class GoogleToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "google_token_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "refresh_token", nullable = false, length = 2048)
    private String refreshToken;

    @Column(name = "access_token",nullable = false,length = 2048)
    private String accessToken;

    private LocalDateTime accessTokenExpiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    public static GoogleToken create(Member member, GoogleTokenDto googleTokenDto) {
        GoogleToken googleToken = new GoogleToken();
        googleToken.setMember(member);
        googleToken.setRefreshToken(googleTokenDto.getRefreshToken());
        googleToken.setAccessToken(googleTokenDto.getAccessToken());
        googleToken.setAccessTokenExpiresAt(googleTokenDto.getAccessTokenExpiresAt());
        return googleToken;
    }
}