package com.example.emojournal.controller;

import com.example.emojournal.auth.AuthTokens;
import com.example.emojournal.auth.GoogleLoginParams;
import com.example.emojournal.dto.AuthorizationCodeRequest;
import com.example.emojournal.service.OAuthLoginService;
import com.example.emojournal.service.oAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/login/oauth2/code")
public class oAuthController {

    private final OAuthLoginService oAuthLoginService;

    @PostMapping("/google")
    public ResponseEntity<AuthTokens> loginGoogle(@RequestBody AuthorizationCodeRequest codeRequest) {

        // 바디에 있는 params 를 받음
        // params 에는 authorizationCode 가 있음
        // 구글 소셜로그인으로 로그인 하는 메서드

        GoogleLoginParams params = new GoogleLoginParams();
        params.setAuthorizationCode(codeRequest.getCode());

        log.info("post google");
        log.info("code : " + params.getAuthorizationCode());
        return ResponseEntity.ok(oAuthLoginService.login(params));
    }

}