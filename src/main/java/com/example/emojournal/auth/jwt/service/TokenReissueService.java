package com.example.emojournal.auth.jwt.service;

import com.example.emojournal.auth.jwt.utils.AuthTokenGenerator;
import com.example.emojournal.auth.jwt.dto.AuthTokens;
import com.example.emojournal.auth.jwt.entity.RefreshToken;
import com.example.emojournal.auth.jwt.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenReissueService {

    private final AuthTokenGenerator authTokenGenerator;

    private final RefreshTokenRepository refreshTokenRepository;

    // access token 재발급
    // refresh token 이
    public Optional<AuthTokens> reissueAccessToken(String refreshToken) {
        log.info("refresh Token : " + refreshToken);

        RefreshToken dbRefreshToken = refreshTokenRepository.findByRefreshToken(refreshToken).orElseThrow(NoSuchElementException::new);

        LocalDateTime expiresAt = dbRefreshToken.getExpiresAt();

        if(LocalDateTime.now().isBefore(expiresAt)) {
            return Optional.of(authTokenGenerator.generate(dbRefreshToken.getMember().getId()));
        }

        return Optional.empty();
    }

}