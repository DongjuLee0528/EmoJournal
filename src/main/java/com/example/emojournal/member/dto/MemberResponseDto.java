package com.example.emojournal.member.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponseDto {

    private Long id;
    private String email;
    private String nickname;
    private String oAuthProvider;
    private String gender;
    private String mbti;
    private String createDate;
}
