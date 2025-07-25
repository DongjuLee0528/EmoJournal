package com.example.emojournal.calendar.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleCalendarEventListResponse {
    private List<GoogleCalendarEventDto> items;
}
