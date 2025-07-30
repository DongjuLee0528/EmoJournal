package com.example.emojournal.calendar.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalendarEventCreateRequest {
    private String summary;           // 일정 제목
    private String description;       // 일정 설명
    private String startDateTime;     // 시작 시간 (ISO format)
    private String endDateTime;       // 종료 시간 (ISO format)
    private boolean allDay;           // 종일 일정 여부
    private String location;          // 장소 (선택사항)
}