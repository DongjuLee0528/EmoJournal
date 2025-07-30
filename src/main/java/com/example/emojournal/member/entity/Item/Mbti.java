package com.example.emojournal.member.entity.Item;

public enum Mbti {
    ISTJ, ISFJ, INFJ, INTJ,
    ISTP, ISFP, INFP, INTP,
    ESTP, ESFP, ENFP, ENTP,
    ESTJ, ESFJ, ENFJ, ENTJ;

    // 소문자 -> 대문자로 변환해서 Enum으로 변환
    public static Mbti from(String value) {
        return Mbti.valueOf(value.trim().toUpperCase());
    }
}