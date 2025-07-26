package com.example.emojournal.calendar.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalendarEventUpdateRequest {
    private String summary;
    private String description;
    private String startDateTime;
    private String endDateTime;
    private boolean allDay;
    private String location;
}