package com.example.emojournal.calendar.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true) // JSON 응답에 정의되지 않은 필드 무시
@ToString
public class GoogleCalendarEventDto {

    private String id;            // 구글 캘린더 이벤트 고유 ID
    private String summary;       // 일정 제목
    private String description;   // 일정 설명
    private Time start;           // 일정 시작 시간 정보
    private Time end;             // 일정 종료 시간 정보

    // Google Calendar API 응답에서 start, end 필드의 구조를 매핑하기 위한 내부 클래스
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ToString
    public static class Time {
        private String dateTime;  // 시간 지정 일정의 시작/종료 시간 (예: 2025-07-27T10:00:00+09:00)
        private String date;      // 종일 일정의 날짜 (예: 2025-07-27)
    }
}
