package com.example.emojournal.auth.jwt.service;

import com.example.emojournal.auth.jwt.entity.embedded.ClientInfo;
import com.example.emojournal.auth.jwt.entity.exception.RefreshTokenAlreadyExistsException;
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

/**
 * 리프레시 토큰의 생명주기를 관리하는 서비스 클래스
 *
 * JWT 리프레시 토큰의 저장, 조회, 업데이트, 삭제 기능을 전담하여 처리합니다.
 * 클라이언트 정보(아이피, 사용자 에이전트)를 기반으로 중복 로그인을 방지하고,
 * 리프레시 토큰의 생명주기를 안전하게 관리합니다.
 *
 * 주요 기능:
 * - 리프레시 토큰의 생성 및 저장
 * - 중복 토큰 처리 (같은 클라이언트에서 여러 번 로그인 시)
 * - 로그아웃 시 토큰 삭제
 * - 회원 기반 토큰 조회 및 삭제
 * - 에이전트와 IP 기반 토큰 중복 방지
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    /** 리프레시 토큰 데이터베이스 접근을 담당하는 JPA 리포지토리 */
    private final RefreshTokenRepository refreshTokenRepository;

    /** JWT 토큰의 유효성 검증을 담당하는 프로바이더 */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 리프레시 토큰을 저장하거나 기존 토큰을 갱신합니다.
     *
     * 같은 클라이언트 정보(아이피, 사용자 에이전트)와 회원으로 기존 토큰이 있는지 확인합니다.
     * 기존 토큰이 없다면 새로 생성하고, 있다면 새로운 토큰으로 갱신하여
     * 중복 로그인을 방지합니다.
     *
     * @param refreshToken 저장하거나 갱신할 리프레시 토큰 엔티티
     * @return 저장되거나 갱신된 리프레시 토큰의 ID
     */
    public Long saveIfNotExists (RefreshToken refreshToken) {

        // 같은 클라이언트 정보와 회원으로 기존 리프레시 토큰 조회
        RefreshToken findRefreshToken = findByIpAddressAndMember(refreshToken.getClientInfo(), refreshToken.getMember());

        if(findRefreshToken == null) {
            // 기존 토큰이 없다면 새로운 리프레시 토큰 생성 및 저장
            return refreshTokenRepository.save(refreshToken).getId();
        }

        // 기존 토큰이 있다면 새로운 값들으로 갱신
        findRefreshToken.setRefreshToken(refreshToken.getRefreshToken());
        findRefreshToken.setCreatedAt(LocalDateTime.now()); // 생성 시간 갱신
        findRefreshToken.setExpiresAt(refreshToken.getExpiresAt()); // 만료 시간 갱신

        return  refreshTokenRepository.save(findRefreshToken).getId();
    }

    /**
     * 회원 ID로 리프레시 토큰 ID를 조회합니다.
     *
     * 주어진 회원 ID에 해당하는 리프레시 토큰을 찾아 해당 토큰의 ID를 반환합니다.
     *
     * @param memberId 조회할 회원의 고유 식별자
     * @return 리프레시 토큰의 ID, 토큰이 없으면 null
     */
    public Long findByMemberId(Long memberId) {
        Optional<RefreshToken> byMemberId = refreshTokenRepository.findByMemberId(memberId);
        return byMemberId.map(RefreshToken::getId).orElse(null);
    }



    /**
     * 클라이언트 정보와 회원 정보로 리프레시 토큰을 조회합니다.
     *
     * IP 주소, 사용자 에이전트 등의 클라이언트 정보와 회원 정보를 조합하여
     * 기존에 등록된 리프레시 토큰이 있는지 확인합니다.
     *
     * @param clientInfo 클라이언트 정보 (아이피, 사용자 에이전트 등)
     * @param member 회원 엔티티
     * @return 찾은 리프레시 토큰, 없으면 null
     */
    private RefreshToken findByIpAddressAndMember(ClientInfo clientInfo, Member member) {
        Optional<RefreshToken> byIpAddressAndMember = refreshTokenRepository.findByClientInfoAndMember(clientInfo, member);
        return byIpAddressAndMember.orElse(null);
    }

    /**
     * 로그아웃 시 리프레시 토큰을 삭제합니다.
     *
     * 주어진 리프레시 토큰 문자열로 데이터베이스에서 토큰을 찾아 삭제합니다.
     * 로그아웃 후에는 토큰을 사용할 수 없도록 보장합니다.
     *
     * @param refreshToken 삭제할 리프레시 토큰 문자열
     * @throws NoSuchElementException 해당 토큰을 데이터베이스에서 찾을 수 없는 경우
     */
    public void logout(String refreshToken) {

        RefreshToken dbRefreshToken = refreshTokenRepository.findByRefreshToken(refreshToken).orElseThrow(NoSuchElementException::new);

        refreshTokenRepository.delete(dbRefreshToken);
    }

    /**
     * 회원 ID로 리프레시 토큰을 삭제합니다.
     *
     * 주어진 회원 ID에 해당하는 모든 리프레시 토큰을 삭제합니다.
     * 주로 새로운 로그인 시 기존 토큰을 정리하거나 계정 비활성화 시 사용됩니다.
     *
     * @param memberId 리프레시 토큰을 삭제할 회원의 고유 식별자
     */
    public void deleteByMemberId(Long memberId) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByMemberId(memberId);
        refreshToken.ifPresent(refreshTokenRepository::delete);
    }
}
