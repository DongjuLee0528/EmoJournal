package com.example.emojournal.calendar.dto;

import lombok.Data;

@Data
public class CalendarEventCreateRequest {
    private String summary;           // 이벤트 제목
    private String description;       // 이벤트 설명
    private String startDateTime;     // 시작 시간 (ISO 8601 format)
    private String endDateTime;       // 종료 시간 (ISO 8601 format)
    private String location;          // 위치 (선택사항)
    private String timeZone;          // 시간대 (기본값: Asia/Seoul)

    // 기본 시간대 설정
    public String getTimeZone() {
        return timeZone != null ? timeZone : "Asia/Seoul";
    }
}