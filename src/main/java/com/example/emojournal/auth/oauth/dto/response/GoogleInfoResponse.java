package com.example.emojournal.auth.oauth.dto.response;

import com.example.emojournal.auth.oauth.utils.OAuthInfoResponse;
import com.example.emojournal.auth.jwt.entity.item.OAuthProvider;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleInfoResponse implements OAuthInfoResponse {

    private String id;

    @JsonProperty("email")
    private String email;

    @JsonProperty("name")
    private String name;

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getNickname() {
        return name;
    }

    @Override
    public OAuthProvider getOAuthProvider() {
        return OAuthProvider.GOOGLE;
    }
}
