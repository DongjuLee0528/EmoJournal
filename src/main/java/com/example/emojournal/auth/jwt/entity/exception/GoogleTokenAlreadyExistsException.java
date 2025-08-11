package com.example.emojournal.auth.jwt.entity.exception;

public class GoogleTokenAlreadyExistsException extends RuntimeException {
    public GoogleTokenAlreadyExistsException(Long memberId) {
        super("현재 구글 토큰에 속한 멤버 아이디가 있는 memberId : " + memberId + " 의 멤버가 이미 존재합니다.");
    }
}
