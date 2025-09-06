package com.example.emojournal.auth.oauth.utils;

import com.example.emojournal.auth.jwt.entity.item.OAuthProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GoogleLoginParams implements OAuthLoginParams {

    private String authorizationCode; // code
    private String redirectUri;       // ✅ 추가
    private String codeVerifier;      // ✅ 추가

    @Override
    public OAuthProvider oAuthProvider() {
        return OAuthProvider.GOOGLE;
    }

    @Override
    public MultiValueMap<String, String> makeBody() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", authorizationCode);
        body.add("grant_type", "authorization_code");   // ✅ 필수
        if (redirectUri != null)   body.add("redirect_uri", redirectUri);     // ✅ 필수
        if (codeVerifier != null)  body.add("code_verifier", codeVerifier);   // ✅ PKCE
        return body;
    }
}
