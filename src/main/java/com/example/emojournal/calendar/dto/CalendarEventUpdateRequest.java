package com.example.emojournal.calendar.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalendarEventUpdateRequest {

    private String summary;         // 일정 제목 (수정 가능)
    private String description;     // 일정 설명 (수정 가능)
    private String startDateTime;   // 일정 시작 시간 (수정 가능, ISO 8601 형식)
    private String endDateTime;     // 일정 종료 시간 (수정 가능, ISO 8601 형식)
    private boolean allDay;         // 종일 여부 (true: 종일, false: 시간 지정)
    private String location;        // 일정 장소 (수정 가능)
}
