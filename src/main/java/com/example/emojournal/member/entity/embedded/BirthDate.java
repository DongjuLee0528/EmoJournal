package com.example.emojournal.member.entity.embedded;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@Getter
@Embeddable
public class BirthDate {

    // getter
    private Integer year;
    private Integer month;
    private Integer day;

    protected BirthDate() {} // JPA 기본 생성자

    public BirthDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    // 편의 메서드: LocalDate 변환
    public LocalDate toLocalDate() {
        return LocalDate.of(year, month, day);
    }

    // 편의 메서드: 나이 계산
    public Optional<Integer> getAge() {
        if (year == null || month == null || day == null) {
            return Optional.empty();
        }
        return Optional.of(Period.between(toLocalDate(), LocalDate.now()).getYears());
    }

}