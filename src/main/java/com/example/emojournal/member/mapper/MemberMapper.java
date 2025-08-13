package com.example.emojournal.member.mapper;

import com.example.emojournal.member.dto.BirthDateDto;
import com.example.emojournal.member.dto.response.MemberResponseDto;
import com.example.emojournal.member.entity.Member;
import com.example.emojournal.member.entity.embedded.BirthDate;

public class MemberMapper {

    static public MemberResponseDto toDto(Member member) {
        Integer age = (member.getBirthDate() != null)
                ? member.getBirthDate().getAge().orElse(null)
                : null;

        return MemberResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .oAuthProvider(safeEnum(member.getOAuthProvider()))
                .createDate(safeToString(member.getCreateDate()))
                .gender(safeEnum(member.getGender()))
                .mbti(safeEnum(member.getMbti()))
                .birthDate(safeBirthDateDto(member.getBirthDate()))
                .age(age)
                .build();
    }

    private static String safeEnum(Enum<?> e) {
        return e != null ? e.toString() : null;
    }

    private static String safeToString(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    private static BirthDateDto safeBirthDateDto(BirthDate bd) {
        return bd != null ? new BirthDateDto(bd.getYear(), bd.getMonth(), bd.getDay()) : null;
    }


}
