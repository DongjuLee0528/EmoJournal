package com.example.emojournal.auth.jwt.utils;

public class AuthenticationContextHolder {

    private static final ThreadLocal<Long> context = new ThreadLocal<>();

    public static Long getContext() {
        return context.get();
    }

    public static void setContext(Long memberId) {
        context.set(memberId);
    }

    public static void clear() {
        context.remove();
    }

}
