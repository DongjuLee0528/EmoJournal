package com.example.emojournal.auth.oauth.utils;

import com.example.emojournal.auth.jwt.entity.item.OAuthProvider;
import org.springframework.util.MultiValueMap;

public interface OAuthLoginParams {
    OAuthProvider oAuthProvider();
    MultiValueMap<String,String> makeBody();
}
