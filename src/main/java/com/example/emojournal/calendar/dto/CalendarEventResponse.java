package com.example.emojournal.calendar.dto;

import lombok.Data;

@Data
public class CalendarEventResponse {
    private String id;               // Google Calendar 이벤트 ID
    private String summary;          // 이벤트 제목
    private String description;      // 이벤트 설명
    private String startDateTime;    // 시작 시간
    private String endDateTime;      // 종료 시간
    private String location;         // 위치
    private String status;           // 이벤트 상태 (confirmed, tentative, cancelled)
    private String created;          // 생성 시간
    private String updated;          // 수정 시간
}