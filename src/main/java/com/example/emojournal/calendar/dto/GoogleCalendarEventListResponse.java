package com.example.emojournal.calendar.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true) // JSON 응답에 정의되지 않은 필드는 무시
public class GoogleCalendarEventListResponse {

    // Google Calendar API로부터 받은 일정 리스트
    private List<GoogleCalendarEventDto> items;
}
