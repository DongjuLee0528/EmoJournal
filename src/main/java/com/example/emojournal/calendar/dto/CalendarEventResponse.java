package com.example.emojournal.calendar.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CalendarEventResponse {

    private String id;              // 구글 캘린더에서 생성된 일정의 고유 ID
    private String summary;         // 일정 제목
    private String description;     // 일정 설명
    private String startDateTime;   // 일정 시작 시간 (ISO 8601 형식 문자열)
    private String endDateTime;     // 일정 종료 시간 (ISO 8601 형식 문자열)
    private boolean allDay;         // 종일 일정 여부
    private String location;        // 일정 장소
    private String status;          // 일정 상태 (예: confirmed, cancelled 등)
}
