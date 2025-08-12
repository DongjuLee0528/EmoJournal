package com.example.emojournal.auth.oauth.repository;

import com.example.emojournal.auth.oauth.entity.GoogleToken;
import com.example.emojournal.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public  interface GoogleTokenRepository extends JpaRepository<GoogleToken,Long> {
    Optional<Long> findByRefreshToken(String refreshToken);

    Optional<GoogleToken> findByMember(Member member);

    Optional<GoogleToken> findByMemberId(Long memberId);
}
