package com.example.emojournal.calendar.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CalendarEventResponse {
    private String id;
    private String summary;
    private String description;
    private String startDateTime;
    private String endDateTime;
    private boolean allDay;
    private String location;
    private String status;
}