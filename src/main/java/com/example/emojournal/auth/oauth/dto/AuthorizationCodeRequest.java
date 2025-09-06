package com.example.emojournal.auth.oauth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AuthorizationCodeRequest {
    private String code;
    private String redirectUri;
    private String codeVerifier;
}