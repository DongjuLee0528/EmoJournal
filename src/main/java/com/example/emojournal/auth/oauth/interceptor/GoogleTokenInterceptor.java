package com.example.emojournal.auth.oauth.interceptor;

import com.example.emojournal.auth.jwt.entity.exception.GoogleTokenNotFoundException;
import com.example.emojournal.auth.jwt.entity.exception.InvalidAccessTokenException;
import com.example.emojournal.auth.jwt.utils.AuthenticationContextHolder;
import com.example.emojournal.auth.jwt.utils.JwtTokenProvider;
import com.example.emojournal.auth.oauth.client.GoogleApiClient;
import com.example.emojournal.auth.oauth.dto.response.GoogleAccessTokenResponse;
import com.example.emojournal.auth.jwt.utils.crypto.CryptoUtil;
import com.example.emojournal.auth.oauth.entity.GoogleToken;
import com.example.emojournal.member.entity.Member;
import com.example.emojournal.auth.oauth.repository.GoogleTokenRepository;
import com.example.emojournal.member.repository.MemberRepository;
import com.example.emojournal.member.service.MemberService;
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

// google access token 재발급 받는 인터셉터
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleTokenInterceptor implements HandlerInterceptor {

    private final GoogleTokenRepository googleTokenRepository;
    private final CryptoUtil cryptoUtil;
    private final GoogleApiClient googleApiClient;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        AuthenticationContextHolder.clear();
        log.debug("AuthenticationContextHolder cleared");
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    // 구글 토큰을 재발급 하는 인터셉터 로직이다
    @Transactional
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
       log.info("preHandle");

        // 필터에서 저장한 memberId
        Long memberId = AuthenticationContextHolder.getContext();

        if(memberId == null) {
            throw new InvalidAccessTokenException();
        }

        // 구글 토큰 memberId를 가진 토큰 찾기
        GoogleToken googleToken = googleTokenRepository.findByMemberId(memberId).orElseThrow(() -> new GoogleTokenNotFoundException(memberId));

        // 구글 토큰의 만료일 가져옴
        LocalDateTime accessTokenExpiresAt = googleToken.getAccessTokenExpiresAt();

        // 구글 토큰에 만료일이 지나지않았다면
        if(LocalDateTime.now().isBefore(accessTokenExpiresAt)) {
            log.info("구글 토큰 발급 안받아도됨");

            AuthenticationContextHolder.setContext(memberId);

            return true;
        } else {
            // 리프레쉬 토큰으로 access token 새로 발급 받기
            log.info("구글 토큰 발급 받아야됨");

            // 디비에있는 구글 리프레쉬 토큰을 디코딩함
            String decryptRefreshToken = cryptoUtil.decrypt(googleToken.getRefreshToken());

            // 구글에 리프레쉬토큰으로 엑세스토큰 요청을보냄
            GoogleAccessTokenResponse googleAccessTokenResponse = googleApiClient.refreshAccessToken(decryptRefreshToken);

            // 받은 엑세스 토큰을 다시 암호화하고 디비에 집어넣음
            googleToken.setAccessToken(cryptoUtil.encrypt(googleAccessTokenResponse.getAccessToken()));
            // 만료일 현재시간 + 한시간
            googleToken.setAccessTokenExpiresAt(LocalDateTime.now().plusHours(1));

            AuthenticationContextHolder.setContext(memberId);

            return true;
        }
    }
}
