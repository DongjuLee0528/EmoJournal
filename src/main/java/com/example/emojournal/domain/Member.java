package com.example.emojournal.domain;

import com.example.emojournal.domain.item.OAuthProvider;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String nickname;

    @Enumerated(EnumType.STRING)
    private OAuthProvider oAuthProvider;

    @Builder
    public Member(Long id, String email, String nickname, OAuthProvider oAuthProvider) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.oAuthProvider = oAuthProvider;
    }
}
