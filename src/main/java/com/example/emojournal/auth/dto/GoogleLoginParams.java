package com.example.emojournal.auth.dto;

import com.example.emojournal.domain.item.OAuthProvider;
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
public class GoogleLoginParams implements OAuthLoginParams{

    private String authorizationCode;

    @Override
    public OAuthProvider oAuthProvider() {
        return OAuthProvider.GOOGLE;
    }

    @Override
    public MultiValueMap<String, String> makeBody() {
        // MultiValueMap 에 대한 설명
        // 하나의 키값에 여러개의 값을 집어넣는다면
        // 그냥 HashMap 은 value 값에 하나의 값만 들어갈수있다.
        // 그와 반대로 MultiValueMap 는 value 에 list 가 들어갈수 있다.
        MultiValueMap<String,String> body = new LinkedMultiValueMap<>();
        body.add("code",authorizationCode);
        return body;
    }
}
