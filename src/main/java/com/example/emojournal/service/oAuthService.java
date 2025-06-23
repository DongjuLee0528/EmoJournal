package com.example.emojournal.service;

import com.example.emojournal.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Service
public class oAuthService {

    private final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    @Value("${oauth2.google.client-id}")
    private String GOOGLE_CLIENT_ID;
    @Value("${oauth2.google.client-secret}")
    private String GOOGLE_CLIENT_SECRET;
    @Value("${oauth2.google.redirect-uri}")
    private String LOGIN_REDIRECT_URL;

    @Autowired
    private MemberRepository memberRepository;

    public ResponseEntity<String> getGoogleAccessToken(String accessCode) {

        RestTemplate restTemplate = new RestTemplate();
        HashMap<String,String> params = new HashMap<>();

        params.put("code",accessCode);
        params.put("client_id",GOOGLE_CLIENT_ID);
        params.put("client_secret",GOOGLE_CLIENT_SECRET);
        params.put("redirect_uri",LOGIN_REDIRECT_URL);
        params.put("grant_type","authorization_code");

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(GOOGLE_TOKEN_URL, params, String.class);

        if(responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity;
        }
        return null;
    }
}