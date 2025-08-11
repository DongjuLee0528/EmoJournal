package com.example.emojournal.auth.oauth.service;

import com.example.emojournal.auth.jwt.dto.AuthTokens;
import com.example.emojournal.auth.jwt.entity.RefreshToken;
import com.example.emojournal.auth.jwt.entity.embedded.ClientInfo;
import com.example.emojournal.auth.jwt.service.RefreshTokenService;
import com.example.emojournal.auth.jwt.utils.UserAgentParser;
import com.example.emojournal.auth.jwt.utils.crypto.CryptoUtil;
import com.example.emojournal.auth.oauth.dto.GoogleTokenDto;
import com.example.emojournal.auth.oauth.dto.LoginResponse;
import com.example.emojournal.auth.oauth.dto.OAuthLoginTokenDto;
import com.example.emojournal.auth.oauth.dto.OAuthTokens;
import com.example.emojournal.auth.oauth.utils.GoogleLoginParams;
import com.example.emojournal.member.entity.Member;
import com.example.emojournal.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthLoginFacadeService {

    private final OAuthLoginService oAuthLoginService;

    private final RefreshTokenService refreshTokenService;

    private final MemberService memberService;

    private final GoogleTokenService googleTokenService;

    private final CryptoUtil cryptoUtil;

    private final UserAgentParser userAgentParser;

    // 자식 메서드,부모 메서드 에러가 뜨면
    // 그대로 롤백
    @Transactional
    public OAuthLoginTokenDto handleOAuthLogin(HttpServletRequest request, GoogleLoginParams params) throws Exception {


        // 받은 code 를 매게변수로 넘겨주고
        // 토큰 객체, memberId 를 받아옴
        LoginResponse loginResponse = oAuthLoginService.login(params);

        // 스프링에서 만든 토큰들
        AuthTokens authTokens = loginResponse.getAuthTokens();

        // 소셜로그인 서버에서 만든 토큰들
        OAuthTokens oAuthTokens = loginResponse.getOAuthTokens();

        log.info("소셜로그인 : " + oAuthTokens.toString());

        // 구글 서버에서 준 access token
        String googleAccessToken = oAuthTokens.getAccessToken();

        // 구글 서버에서 준 refresh token
        String googleRefreshToken = oAuthTokens.getRefreshToken();

        // 스프링에서 만든 access token
        String accessToken = authTokens.getAccessToken();

        // 스프링 서버에서 만드는 refresh token
        String refreshToken = UUID.randomUUID().toString();

        // memberId 로 member 를찾아야됨
        Member member = memberService.findMemberById(loginResponse.getMemberId());


        // 만약 googleAccessToken,googleRefreshTokne 이 없다면
        // 이말은 로그인이 처음이 아니라는 뜻
        if(googleAccessToken != null && googleRefreshToken != null) {

            // 구글 토큰 생성자
            GoogleTokenDto googleTokenDto = GoogleTokenDto.builder()
                    .accessToken(cryptoUtil.encrypt(googleAccessToken))
                    .refreshToken(cryptoUtil.encrypt(googleRefreshToken))
                    // 구글 토큰에 엑세스 토큰 만료일
                    .accessTokenExpiresAt(LocalDateTime.now().plusHours(1))
                    .memberId(member.getId())
                    .build();

            log.info("googleTokenInfo :  " + googleTokenDto);

            // 구글 토큰 저장
            googleTokenService.saveIfNotExists(member.getId(),googleTokenDto);
        }

//        log.info("ip address : " + request.getRemoteAddr());

        // 로그인한 상태에서 refresh token 을 갱신해야되나 말아야 되나?
        // referesh token 이 존재한다면 이걸 새로 갈아껴야되나
        // 없다면 당연이 갈아껴야되고


        // 스프링 서버에서 만든 토큰 저장
        // 만약 멤버가 이미있다면 null 리턴
        // 아니라면 Long 타입형 id 리턴

        String userAgent = userAgentParser.extractBrowserInfo(request.getHeader("User-Agent"));
        String remoteAddr = request.getRemoteAddr();

        log.info("remoteAddr :  " + remoteAddr);
        log.info("userAgent :  " + userAgent);

        ClientInfo clientInfo = new ClientInfo(userAgent, remoteAddr);

        refreshTokenService.saveIfNotExists(RefreshToken.create(refreshToken, LocalDateTime.now().plusWeeks(1),clientInfo , member));

        log.info("refreshToken : " + refreshToken);

        return OAuthLoginTokenDto.builder()
                .refreshToken(refreshToken)
                .authTokens(authTokens)
                .build();
    }

}
