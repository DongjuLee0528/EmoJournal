package com.example.emojournal.calendar;

import com.example.emojournal.calendar.dto.GoogleCalendarEventListResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest

class GoogleCalendarClientTest {

    @Autowired
    private GoogleCalendarClient googleCalendarClient;

    @Test
    public void 구글캘린더_가져오기() {
        String accessToken = "";

        String timeMin = "2025-07-01T18:18";
        String timeMax = "2025-07-25T19:19";

        timeMax = 변환메서드(timeMax);

        timeMin = 변환메서드(timeMin);

        System.out.println("timeMax = " + timeMax);
        System.out.println("timeMin = " + timeMin);


        GoogleCalendarEventListResponse response =
                googleCalendarClient.getCalendarEvents(accessToken, timeMin, timeMax);

        System.out.println("response = " + response);

        response.getItems().forEach(evnet -> {
            System.out.println("evnet : " + evnet.toString());
        });
    }

    private String 변환메서드(String time) {
        LocalDateTime localDateTime = LocalDateTime.parse(time);

        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("Asia/Seoul"));

        Instant utcInstant  = zonedDateTime.toInstant();

        return utcInstant.toString();
    }
}