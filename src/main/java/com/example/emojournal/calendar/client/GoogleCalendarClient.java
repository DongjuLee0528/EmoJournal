package com.example.emojournal.calendar.client;

import com.example.emojournal.calendar.dto.GoogleCalendarEventCreateRequest;
import com.example.emojournal.calendar.dto.GoogleCalendarEventListResponse;
import com.example.emojournal.calendar.dto.GoogleCalendarEventDto;
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



    // 일정 추가
    public GoogleCalendarEventDto createEvent(String accessToken, GoogleCalendarEventCreateRequest request) {
        HttpHeaders headers = createHeaders(accessToken);
        HttpEntity<GoogleCalendarEventCreateRequest> entity = new HttpEntity<>(request, headers);

        log.info("Creating event: {}", request);

        ResponseEntity<GoogleCalendarEventDto> response = restTemplate.exchange(
                GOOGLE_CALENDAR_EVENTS_URL,
                HttpMethod.POST,
                entity,
                GoogleCalendarEventDto.class
        );

        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
            log.info("Event created successfully: {}", response.getBody());
            return response.getBody();
        }

        throw new RuntimeException("Failed to create event in Google Calendar.");
    }

    // 일정 수정
    public GoogleCalendarEventDto updateEvent(String accessToken, String eventId, GoogleCalendarEventCreateRequest request) {
        String url = GOOGLE_CALENDAR_EVENTS_URL + "/" + eventId;

        HttpHeaders headers = createHeaders(accessToken);
        HttpEntity<GoogleCalendarEventCreateRequest> entity = new HttpEntity<>(request, headers);

        log.info("Updating event {}: {}", eventId, request);

        ResponseEntity<GoogleCalendarEventDto> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                entity,
                GoogleCalendarEventDto.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            log.info("Event updated successfully: {}", response.getBody());
            return response.getBody();
        }

        throw new RuntimeException("Failed to update event in Google Calendar.");
    }

    // 일정 삭제
    public void deleteEvent(String accessToken, String eventId) {
        String url = GOOGLE_CALENDAR_EVENTS_URL + "/" + eventId;

        HttpHeaders headers = createHeaders(accessToken);
        HttpEntity<Object> entity = new HttpEntity<>(headers);

        log.info("Deleting event: {}", eventId);

        ResponseEntity<Void> response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                entity,
                Void.class
        );

        if (response.getStatusCode() == HttpStatus.NO_CONTENT || response.getStatusCode() == HttpStatus.OK) {
            log.info("Event deleted successfully: {}", eventId);
            return;
        }

        throw new RuntimeException("Failed to delete event from Google Calendar.");
    }

    // 단일 일정 조회
    public GoogleCalendarEventDto getEvent(String accessToken, String eventId) {
        String url = GOOGLE_CALENDAR_EVENTS_URL + "/" + eventId;

        HttpHeaders headers = createHeaders(accessToken);
        HttpEntity<Object> entity = new HttpEntity<>(headers);

        log.info("Fetching event: {}", eventId);

        ResponseEntity<GoogleCalendarEventDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                GoogleCalendarEventDto.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }

        throw new RuntimeException("Failed to fetch event from Google Calendar.");
    }

    // 헤더 생성 공통 메서드
    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}