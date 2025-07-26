package com.example.emojournal.auth.oauth.utils;

import com.example.emojournal.auth.jwt.entity.item.OAuthProvider;

public interface OAuthInfoResponse {

    String getEmail();

    String getNickname();

    OAuthProvider getOAuthProvider();
}
