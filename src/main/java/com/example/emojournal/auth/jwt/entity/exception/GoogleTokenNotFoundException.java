package com.example.emojournal.auth.jwt.entity.exception;

public class GoogleTokenNotFoundException extends RuntimeException {
    public GoogleTokenNotFoundException(Long memberId) {
        super("해당 멤버("+memberId + ")의 구글 토큰이 존재하지 않습니다.");
    }
}
