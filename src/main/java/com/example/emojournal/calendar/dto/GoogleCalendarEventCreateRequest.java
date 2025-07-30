package com.example.emojournal.calendar.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GoogleCalendarEventCreateRequest {
    private String summary;
    private String description;
    private String location;
    private EventDateTime start;
    private EventDateTime end;

    @Getter
    @Builder
    public static class EventDateTime {
        private String dateTime;  // "2025-04-15T10:00:00+09:00"
        private String date;      // "2025-04-15" (종일 일정용)
        private String timeZone;  // "Asia/Seoul"
    }
}