package com.example.emojournal.member.dto.response;

import com.example.emojournal.member.dto.BirthDateDto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MemberResponseDto {

    private Long id;
    private String email;
    private String nickname;
    private String oAuthProvider;
    private String gender;
    private String mbti;
    private String createDate;

    // 새로 추가할 필드들
    private BirthDateDto birthDate;  // 생년월일
    private Integer age;          // 나이 (계산된 값)
    private Boolean profileCompleted;  // 프로필 완성 여부
}