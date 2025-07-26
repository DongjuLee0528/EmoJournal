package com.example.emojournal.auth.jwt.entity;

import com.example.emojournal.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String refreshToken;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean revoked = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 정적 팩토리 메서드
    public static RefreshToken create(String refreshToken, LocalDateTime expiresAt, Member member) {
        RefreshToken token = new RefreshToken();
        token.refreshToken = refreshToken;
        token.createdAt = LocalDateTime.now();
        token.expiresAt = expiresAt;
        token.revoked = false;
        token.member = member;
        return token;
    }

    // 토큰 만료 여부 확인 (도메인 로직)
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    // 토큰 폐기 로직
    public void revoke() {
        this.revoked = true;
    }

}
