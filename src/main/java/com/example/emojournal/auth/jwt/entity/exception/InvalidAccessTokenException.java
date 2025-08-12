package com.example.emojournal.auth.jwt.entity.exception;

public class InvalidAccessTokenException extends RuntimeException {
    public InvalidAccessTokenException() {
        super("유효하지 않은 토큰 입니다.");
    }
}
