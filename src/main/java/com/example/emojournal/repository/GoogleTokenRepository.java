package com.example.emojournal.repository;

import com.example.emojournal.domain.GoogleToken;
import com.example.emojournal.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public  interface GoogleTokenRepository extends JpaRepository<GoogleToken,Long> {
    Optional<Long> findByRefreshToken(String refreshToken);

    Optional<GoogleToken> findByMember(Member member);
}
