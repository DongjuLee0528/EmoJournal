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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


// OAuthLoginService 의 책임
// Authorization code 를 받아서 로그인 하는 책임을 가지고 있음
@Service
@RequiredArgsConstructor
public class OAuthLoginService {

    // jpa 에서 member 와 관련된 클래스
    private final MemberRepository memberRepository;

    // 토큰 만드는 생성 클래스
    private final AuthTokenGenerator authTokenGenerator;

    // OAuth 정보들을 요청하는 서비스
    private final RequestOAuthInfoService requestOAuthInfoService;

    // OAuthLoginParams 인터페이스 를 구현한 GoogleLoginParams
    public LoginResponse login(OAuthLoginParams params) {

        // 사용자 정보를 dto 로 가져옴
        OAuthLoginResponse oAuthLoginResponse = requestOAuthInfoService.request(params);

        OAuthTokens oAuthTokens = oAuthLoginResponse.getOAuthTokens();

        // 가져온 정보 dto 를 가지고 db 에서 정보를 찾거나 or 만들어줌
        Long memberId = findOrCreateMember(oAuthLoginResponse.getOAuthInfoResponse());

        AuthTokens authTokens = authTokenGenerator.generate(memberId);

        return new LoginResponse(memberId, authTokens,oAuthTokens);
    }

    // 매게변수로 oauth 정보를 받음
    private Long findOrCreateMember(OAuthInfoResponse oAuthInfoResponse) {
        // 찾는 email 이 없다면 profile 을 생성하고
        // 있다면 유저 id 를 가져옴
        // 둘다 마지막에 id 를 가져오는 건 똑같음
        return memberRepository.findByEmail(oAuthInfoResponse.getEmail())
                .map(Member::getId)
                .orElseGet(() -> newMember(oAuthInfoResponse));
    }

    private Long newMember(OAuthInfoResponse oAuthInfoResponse) {
        Member member = Member.builder()
                .email(oAuthInfoResponse.getEmail())
                .nickname(oAuthInfoResponse.getNickname())
                .oAuthProvider(oAuthInfoResponse.getOAuthProvider())
                .build();

        return memberRepository.save(member).getId();
    }

}
