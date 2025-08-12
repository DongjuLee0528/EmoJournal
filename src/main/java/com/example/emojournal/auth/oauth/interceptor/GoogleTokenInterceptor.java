package com.example.emojournal.auth.oauth.interceptor;

import com.example.emojournal.auth.oauth.client.GoogleApiClient;
import com.example.emojournal.auth.oauth.dto.response.GoogleAccessTokenResponse;
import com.example.emojournal.auth.jwt.utils.crypto.CryptoUtil;
import com.example.emojournal.auth.oauth.entity.GoogleToken;
import com.example.emojournal.member.entity.Member;
import com.example.emojournal.auth.oauth.repository.GoogleTokenRepository;
import com.example.emojournal.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleTokenInterceptor implements HandlerInterceptor {

    private final MemberRepository memberRepository;
    private final GoogleTokenRepository googleTokenRepository;
    private final CryptoUtil cryptoUtil;
    private final GoogleApiClient googleApiClient;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Transactional
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Long memberId = (Long)request.getAttribute("memberId");

        // 멤버 찾기
        Member member = memberRepository.findById(memberId).orElseThrow(NoSuchElementException::new);

        // 구글 토큰 member 와 같은 거 찾기
        GoogleToken googleToken = googleTokenRepository.findByMember(member).orElseThrow(NoSuchElementException::new);

        LocalDateTime accessTokenExpiresAt = googleToken.getAccessTokenExpiresAt();

        if(LocalDateTime.now().isBefore(accessTokenExpiresAt)) {
            log.info("구글 토큰 발급 안받아도됨");
            return true;
        } else {
            // 리프레쉬 토큰으로 access token 새로 발급 받기
            log.info("구글 토큰 발급 받아야됨");
            String decryptRefreshToken = cryptoUtil.decrypt(googleToken.getRefreshToken());

            GoogleAccessTokenResponse googleAccessTokenResponse = googleApiClient.refreshAccessToken(decryptRefreshToken);

            googleToken.setAccessToken(cryptoUtil.encrypt(googleAccessTokenResponse.getAccessToken()));
            googleToken.setAccessTokenExpiresAt(LocalDateTime.now().plusHours(1));

            return true;
        }
    }
}
