package com.example.emojournal.auth.jwt.repository;

import com.example.emojournal.member.entity.Member;
import com.example.emojournal.auth.jwt.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    Optional<RefreshToken> findByMember(Member member);

    Optional<RefreshToken> findByIpAddressAndMember(String ipAddress, Member member);

}
