package com.example.emojournal.auth.oauth.dto.response;


import com.example.emojournal.auth.oauth.utils.OAuthInfoResponse;
import com.example.emojournal.auth.oauth.dto.OAuthTokens;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OAuthLoginResponse {

    private OAuthInfoResponse oAuthInfoResponse;

    private OAuthTokens oAuthTokens;
}
