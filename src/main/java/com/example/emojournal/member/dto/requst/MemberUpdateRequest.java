package com.example.emojournal.member.dto.requst;

import lombok.Data;

@Data
public class MemberUpdateRequest {
    private String name;
    private String nickname;
    private String mbti;
    private String gender;
}