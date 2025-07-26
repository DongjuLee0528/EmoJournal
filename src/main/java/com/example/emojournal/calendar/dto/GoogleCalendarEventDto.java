package com.example.emojournal.calendar.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class GoogleCalendarEventDto {

    private String id;
    private String summary;
    private String description;
    private Time start;
    private Time end;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ToString
    public static class Time {
        private String dateTime;
        private String date;
    }
}