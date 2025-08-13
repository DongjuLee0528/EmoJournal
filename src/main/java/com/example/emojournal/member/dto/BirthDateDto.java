package com.example.emojournal.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BirthDateDto {

    private int year;
    private int month;
    private int day;

    public BirthDateDto() {}

    public BirthDateDto(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }
}