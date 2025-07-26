package com.example.emojournal.auth.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthTokens {

    private String accessToken;
    private String grantType;
    private Long expiresIn;

    public static AuthTokens of(String accessToken,String grantType,Long expiresIn) {
        return new AuthTokens(accessToken,grantType,expiresIn);
    }

}
