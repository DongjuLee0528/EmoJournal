package com.example.emojournal.auth.oauth.service;

import com.example.emojournal.auth.oauth.client.RequestOAuthInfoService;
import com.example.emojournal.auth.jwt.dto.AuthTokens;
import com.example.emojournal.auth.jwt.utils.AuthTokenGenerator;
import com.example.emojournal.auth.oauth.dto.LoginResponse;
import com.example.emojournal.auth.oauth.dto.OAuthTokens;
import com.example.emojournal.auth.oauth.dto.response.OAuthLoginResponse;
import com.example.emojournal.auth.oauth.utils.OAuthInfoResponse;
import com.example.emojournal.auth.oauth.utils.OAuthLoginParams;
import com.example.emojournal.member.entity.Member;
import com.example.emojournal.member.repository.MemberRepository;
import com.example.emojournal.auth.jwt.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * OAuth 로그인 처리를 담당하는 핵심 서비스 클래스
 *
 * OAuth 2.0 인증코드 플로우를 통해 사용자 인증을 처리하고,
 * 새로운 회원 등록 또는 기존 회원 로그인을 수행합니다.
 * JWT 토큰 생성과 리프레시 토큰 관리도 함께 처리합니다.
 *
 * 주요 기능:
 * - OAuth 인증코드로 사용자 정보 획듵
 * - 신규 회원 등록 및 기존 회원 인증
 * - JWT 액세스 토큰 및 리프레시 토큰 발급
 * - 중복 로그인 방지를 위한 기존 토큰 정리
 * - OAuth 액세스 토큰 및 리프레시 토큰 관리
 *
 * 로그인 플로우:
 * 1. OAuth 인증코드로 사용자 정보 요청
 * 2. 이메일 기반 기존 회원 조회 또는 신규 회원 등록
 * 3. 기존 리프레시 토큰 삭제 (중복 로그인 방지)
 * 4. 새로운 JWT 토큰 생성 및 리프레시 토큰 저장
 * 5. 로그인 응답 반환
 */
@Service
@RequiredArgsConstructor
public class OAuthLoginService {

    /** 회원 정보 조회 및 저장을 담당하는 JPA 리포지토리 */
    private final MemberRepository memberRepository;

    /** JWT 액세스 토큰과 리프레시 토큰 생성을 담당하는 유틸리티 */
    private final AuthTokenGenerator authTokenGenerator;

    /** OAuth 제공자로부터 사용자 정보를 요청하는 서비스 */
    private final RequestOAuthInfoService requestOAuthInfoService;

    /** 리프레시 토큰의 생명주기를 관리하는 서비스 */
    private final RefreshTokenService refreshTokenService;

    /**
     * OAuth 로그인 처리를 수행합니다.
     *
     * OAuth 2.0 인증코드 플로우를 통해 사용자 인증을 수행하고,
     * 새로운 회원 등록 또는 기존 회원 로그인을 처리합니다.
     * 또한 JWT 토큰을 생성하여 인증 상태를 유지합니다.
     *
     * @param params OAuth 로그인 파라미터 (인증코드 포함)
     * @return 로그인 응답 (회원ID, JWT토큰, OAuth토큰 포함)
     * @throws RuntimeException OAuth 인증 실패 또는 토큰 생성 실패 시
     */
    public LoginResponse login(OAuthLoginParams params) {

        // OAuth 제공자로부터 사용자 정보와 토큰 정보 획듵
        OAuthLoginResponse oAuthLoginResponse = requestOAuthInfoService.request(params);

        OAuthTokens oAuthTokens = oAuthLoginResponse.getOAuthTokens();

        // 이메일 기반으로 기존 회원 조회 또는 신규 회원 등록
        Long memberId = findOrCreateMember(oAuthLoginResponse.getOAuthInfoResponse());

        // 중복 로그인 방지를 위한 기존 리프레시 토큰 삭제
        refreshTokenService.deleteByMemberId(memberId);

        // 새로운 JWT 액세스 토큰과 리프레시 토큰 생성
        AuthTokens authTokens = authTokenGenerator.generate(memberId);

        return new LoginResponse(memberId, authTokens,oAuthTokens);
    }

    /**
     * OAuth 사용자 정보로 기존 회원을 조회하거나 신규 회원을 등록합니다.
     *
     * 이메일을 기준으로 데이터베이스에서 기존 회원을 찾고,
     * 없으면 OAuth 제공자 정보로 새로운 회원을 등록합니다.
     *
     * @param oAuthInfoResponse OAuth 제공자로부터 받은 사용자 정보
     * @return 찾거나 생성된 회원의 ID
     */
    private Long findOrCreateMember(OAuthInfoResponse oAuthInfoResponse) {
        return memberRepository.findByEmail(oAuthInfoResponse.getEmail())
                // 기존 회원이 있다면 해당 회원 ID 반환
                .map(Member::getId)
                // 기존 회월이 없다면 새로운 회원 생성 후 ID 반환
                .orElseGet(() -> newMember(oAuthInfoResponse));
    }

    /**
     * 새로운 회원을 등록합니다.
     *
     * OAuth 제공자로부터 받은 사용자 정보를 이용하여
     * 데이터베이스에 새로운 회원 레코드를 생성합니다.
     *
     * @param oAuthInfoResponse OAuth 제공자로부터 받은 사용자 정보
     * @return 생성된 회원의 ID
     */
    private Long newMember(OAuthInfoResponse oAuthInfoResponse) {
        Member member = Member.builder()
                .email(oAuthInfoResponse.getEmail())
                .nickname(oAuthInfoResponse.getNickname())
                .oAuthProvider(oAuthInfoResponse.getOAuthProvider())
                .build();

        return memberRepository.save(member).getId();
    }

}
