package com.example.emojournal.calendar;

import com.example.emojournal.calendar.dto.GoogleCalendarEventListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleCalendarClient {

    private static final String GOOGLE_CALENDAR_EVENTS_URL =
            "https://www.googleapis.com/calendar/v3/calendars/primary/events";

    private final RestTemplate restTemplate = new RestTemplate();

    public GoogleCalendarEventListResponse getCalendarEvents(String accessToken,String timeMin,String timeMax) {
        String url = UriComponentsBuilder.fromHttpUrl(GOOGLE_CALENDAR_EVENTS_URL)
                .queryParam("timeMin", timeMin)
                .queryParam("timeMax", timeMax)
                .queryParam("singleEvents", true)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Object> entity = new HttpEntity<>(headers);

        log.info("access token : " + accessToken);

        log.info("timeMin : " + timeMin);

        log.info("timeMax : " + timeMax);

        ResponseEntity<GoogleCalendarEventListResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                GoogleCalendarEventListResponse.class
        );

        if(response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }

        throw new RuntimeException("Failed to fetch events from Google Calendar.");

    }
}
