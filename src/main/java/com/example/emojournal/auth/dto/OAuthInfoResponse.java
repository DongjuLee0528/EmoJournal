package com.example.emojournal.auth.dto;

import com.example.emojournal.domain.item.OAuthProvider;

public interface OAuthInfoResponse {

    String getEmail();

    String getNickname();

    OAuthProvider getOAuthProvider();
}
