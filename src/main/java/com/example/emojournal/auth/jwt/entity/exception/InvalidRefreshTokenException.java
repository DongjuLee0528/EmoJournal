package com.example.emojournal.auth.jwt.entity.exception;

public class InvalidRefreshTokenException extends RuntimeException{

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
