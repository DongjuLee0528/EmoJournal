package com.example.emojournal.auth.jwt.service;

import com.example.emojournal.auth.jwt.utils.JwtTokenProvider;
import com.example.emojournal.auth.jwt.entity.RefreshToken;
import com.example.emojournal.auth.jwt.repository.RefreshTokenRepository;
import com.example.emojournal.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;


// DB 저장, 만료 확인, 삭제, 조회 등 refresh token 자체 관리 전담
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtTokenProvider jwtTokenProvider;

    public Long saveIfNotExists (RefreshToken refreshToken) {

        // IP 와 Member 로 값을 찾았는데 null 일 경우
        if(findByIpAddressAndMember(refreshToken.getIpAddress(),refreshToken.getMember()) == null) {
            return refreshTokenRepository.save(refreshToken).getId();
        }

        return null;
    }

    private RefreshToken findByIpAddressAndMember(String ipAddress, Member member) {
        Optional<RefreshToken> byIpAddressAndMember = refreshTokenRepository.findByIpAddressAndMember(ipAddress, member);
        // Optional 문법 설명 orElse() 는
        // byIpAddressAndMember 의 값이 없다면 orElse 안에 있는 값이
        // 리턴된다
        return byIpAddressAndMember.orElse(null);
    }

    public void logout(String refreshToken) {

        RefreshToken dbRefreshToken = refreshTokenRepository.findByRefreshToken(refreshToken).orElseThrow(NoSuchElementException::new);

        refreshTokenRepository.delete(dbRefreshToken);
    }
}
