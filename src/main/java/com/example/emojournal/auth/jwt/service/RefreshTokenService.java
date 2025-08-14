package com.example.emojournal.auth.jwt.service;

import com.example.emojournal.auth.jwt.entity.embedded.ClientInfo;
import com.example.emojournal.auth.jwt.utils.JwtTokenProvider;
import com.example.emojournal.auth.jwt.entity.RefreshToken;
import com.example.emojournal.auth.jwt.repository.RefreshTokenRepository;
import com.example.emojournal.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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


        // refresh token 의 IP 와 member 를 갖고있는 걸 찾는다
        RefreshToken findRefreshToken = findByIpAddressAndMember(refreshToken.getClientInfo(), refreshToken.getMember());
        // 없다면
        if(findRefreshToken == null) {
            // 새로운 리프레쉬 토큰 저장
            return refreshTokenRepository.save(refreshToken).getId();
        }

        // 있다면
        // 리프레쉬 토큰 새걸로 갈아끼기
        findRefreshToken.setRefreshToken(refreshToken.getRefreshToken());
        // 생성일 현재 시간으로 갱신
        findRefreshToken.setCreatedAt(LocalDateTime.now());
        // 만료일 갱신
        findRefreshToken.setExpiresAt(refreshToken.getExpiresAt());

        return  refreshTokenRepository.save(findRefreshToken).getId();
    }

    public Long findByMemberId(Long memberId) {
        Optional<RefreshToken> byMemberId = refreshTokenRepository.findByMemberId(memberId);
        return byMemberId.map(RefreshToken::getId).orElse(null);
    }



    private RefreshToken findByIpAddressAndMember(ClientInfo clientInfo, Member member) {
        Optional<RefreshToken> byIpAddressAndMember = refreshTokenRepository.findByClientInfoAndMember(clientInfo, member);
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
