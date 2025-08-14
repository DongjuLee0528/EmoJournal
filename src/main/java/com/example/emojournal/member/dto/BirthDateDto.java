package com.example.emojournal.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BirthDateDto {

    @NotNull(message = "년도는 필수입니다.")
    private Integer year;

    @NotNull(message = "월은 필수입니다.")
    private Integer month;

    @NotNull(message = "일은 필수입니다.")
    private Integer day;

    public BirthDateDto() {}

    public BirthDateDto(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }
}