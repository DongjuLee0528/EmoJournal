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

    // Google Calendar API의 기본 URL (기본 캘린더 사용)
    private static final String GOOGLE_CALENDAR_EVENTS_URL =
            "https://www.googleapis.com/calendar/v3/calendars/primary/events";

    // HTTP 요청을 위한 RestTemplate
    private final RestTemplate restTemplate = new RestTemplate();

    // 캘린더 이벤트 목록 조회 (기간: timeMin ~ timeMax)
    public GoogleCalendarEventListResponse getCalendarEvents(String accessToken, String timeMin, String timeMax) {
        // 요청 URL 구성
        String url = UriComponentsBuilder.fromHttpUrl(GOOGLE_CALENDAR_EVENTS_URL)
                .queryParam("timeMin", timeMin)
                .queryParam("timeMax", timeMax)
                .queryParam("singleEvents", true) // 반복 일정 개별 항목으로 처리
                .toUriString();

        // Authorization 헤더 설정 (Bearer 토큰)
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Object> entity = new HttpEntity<>(headers);

        log.info("access token : " + accessToken);
        log.info("timeMin : " + timeMin);
        log.info("timeMax : " + timeMax);

        // GET 요청 전송
        ResponseEntity<GoogleCalendarEventListResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                GoogleCalendarEventListResponse.class
        );

        // 응답 성공 시 데이터 반환
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }

        // 실패 시 예외 처리
        throw new RuntimeException("Failed to fetch events from Google Calendar.");
    }

    // 일정 생성
    public GoogleCalendarEventDto createEvent(String accessToken, GoogleCalendarEventCreateRequest request) {
        HttpHeaders headers = createHeaders(accessToken);
        HttpEntity<GoogleCalendarEventCreateRequest> entity = new HttpEntity<>(request, headers);

        log.info("Creating event: {}", request);

        // POST 요청으로 일정 생성
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

        // PUT 요청으로 일정 수정
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

        // DELETE 요청으로 일정 삭제
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

        // GET 요청으로 단일 일정 조회
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

    // 공통적으로 사용되는 HTTP 헤더 생성 메서드
    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken); // 인증 토큰
        headers.setContentType(MediaType.APPLICATION_JSON); // 요청 Content-Type
        headers.setAccept(List.of(MediaType.APPLICATION_JSON)); // 응답 Accept
        return headers;
    }
}
