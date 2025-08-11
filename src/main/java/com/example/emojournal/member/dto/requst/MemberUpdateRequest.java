package com.example.emojournal.member.dto.requst;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.Period;

@Data
public class MemberUpdateRequest {

    private String name;

    @Size(min = 2, max = 20, message = "닉네임은 2-20자 사이여야 합니다")
    private String nickname;

    private String mbti;
    private String gender;

    // 새로 추가할 생년월일 필드
    @NotNull(message = "생년월일은 필수입니다")
    @Past(message = "생년월일은 과거 날짜여야 합니다")
    private LocalDate birthDate;

    // 만 14세 이상 검증
    @AssertTrue(message = "만 14세 이상만 이용 가능합니다")
    public boolean isAgeValid() {
        if (birthDate == null) return false;
        return Period.between(birthDate, LocalDate.now()).getYears() >= 14;
    }

    // 나이 계산 메서드
    public int getAge() {
        if (birthDate == null) return 0;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}