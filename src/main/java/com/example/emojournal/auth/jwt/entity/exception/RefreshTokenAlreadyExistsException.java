package com.example.emojournal.auth.jwt.entity.exception;

public class RefreshTokenAlreadyExistsException extends RuntimeException {
    public RefreshTokenAlreadyExistsException(Long memberId) {
        super("리프레쉬 토큰이 이미 memberId : " + memberId +  "가 이미 존재합니다.");
    }
}
