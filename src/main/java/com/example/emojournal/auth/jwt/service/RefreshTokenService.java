package com.example.emojournal.auth.jwt.service;

import com.example.emojournal.auth.jwt.utils.JwtTokenProvider;
import com.example.emojournal.auth.jwt.entity.RefreshToken;
import com.example.emojournal.auth.jwt.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;


// DB 저장, 만료 확인, 삭제, 조회 등 refresh token 자체 관리 전담
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtTokenProvider jwtTokenProvider;

    public Long saveRefreshToken(RefreshToken refreshToken) {
        return refreshTokenRepository.save(refreshToken).getId();
    }


    public void logout(String refreshToken) {

        RefreshToken dbRefreshToken = refreshTokenRepository.findByRefreshToken(refreshToken).orElseThrow(NoSuchElementException::new);

        refreshTokenRepository.delete(dbRefreshToken);
    }
}
