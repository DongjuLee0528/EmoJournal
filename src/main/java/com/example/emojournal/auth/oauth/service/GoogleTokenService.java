package com.example.emojournal.auth.oauth.service;

import com.example.emojournal.auth.jwt.entity.exception.GoogleTokenAlreadyExistsException;
import com.example.emojournal.auth.oauth.entity.GoogleToken;
import com.example.emojournal.member.entity.Member;
import com.example.emojournal.auth.oauth.dto.GoogleTokenDto;
import com.example.emojournal.auth.oauth.repository.GoogleTokenRepository;
import com.example.emojournal.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleTokenService {

    private final GoogleTokenRepository googleTokenRepository;

    private final MemberRepository memberRepository;

    // 1. 멤버 아이디와 googleToken dto 를 받는다
    // 2. 멤버 아이디로 google token 을 찾는다.
    // 3. 존재한다면 ?
    // 4. 그냥 놔두고
    // 5. 존재하지 않는다면 새로 만들어준다

    public void saveIfNotExists(Long memberId, GoogleTokenDto googleTokenDto) {
        // memberId로 멤버를 찾는다
        Member member = memberRepository.findById(memberId).orElseThrow();
        // 멤버 아이디로 구글 토큰을 찾는다
        Optional<GoogleToken> googleToken = googleTokenRepository.findByMemberId(member.getId());
        // 구글 토큰이 존재한다면?
        if (googleToken.isPresent()) {
            // 예외처리
            throw new GoogleTokenAlreadyExistsException(memberId);
        }
        // 구글 토큰저장
        googleTokenRepository.save(GoogleToken.create(member,googleTokenDto));
    }

    public Long findByMemberId(Long memberId) {
        Optional<GoogleToken> byMemberId = googleTokenRepository.findByMemberId(memberId);
        return byMemberId.map(GoogleToken::getId).orElse(null);
    }
}
