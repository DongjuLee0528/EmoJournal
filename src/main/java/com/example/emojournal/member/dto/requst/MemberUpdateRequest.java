package com.example.emojournal.member.dto.requst;

import com.example.emojournal.member.dto.BirthDateDto;
import lombok.Data;

@Data
public class MemberUpdateRequest {
    private String nickname;
    private String mbti;
    private String gender;
    private BirthDateDto birthDate;
}