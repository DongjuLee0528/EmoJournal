package com.example.emojournal.member.entity;

import com.example.emojournal.auth.jwt.entity.RefreshToken;
import com.example.emojournal.auth.jwt.entity.item.OAuthProvider;
import com.example.emojournal.member.dto.MemberResponseDto;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String email;
    private String nickname;

    @Enumerated(EnumType.STRING)
    private OAuthProvider oAuthProvider;

    @OneToMany(mappedBy = "member")
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @Builder
    public Member(Long id, String email, String nickname, OAuthProvider oAuthProvider) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.oAuthProvider = oAuthProvider;
    }

    public static MemberResponseDto fromEntity(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .oAuthProvider(member.getOAuthProvider().toString())
                .build();
    }

}
