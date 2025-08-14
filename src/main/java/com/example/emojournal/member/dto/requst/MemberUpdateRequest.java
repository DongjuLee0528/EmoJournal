package com.example.emojournal.member.dto.requst;

import com.example.emojournal.member.dto.BirthDateDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberUpdateRequest {
    @NotBlank
    private String nickname;
    @NotBlank
    private String mbti;
    @NotBlank
    private String gender;
    @NotNull
    @Valid
    private BirthDateDto birthDate;
}