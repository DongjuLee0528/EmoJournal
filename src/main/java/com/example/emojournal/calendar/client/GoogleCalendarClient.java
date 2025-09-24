package com.example.emojournal.calendar.client;

import com.example.emojournal.calendar.dto.CalendarEventCreateRequest;
import com.example.emojournal.calendar.dto.CalendarEventResponse;
import com.example.emojournal.calendar.dto.CalendarEventUpdateRequest;
import com.example.emojournal.calendar.dto.GoogleCalendarEventDto;
import com.example.emojournal.calendar.dto.GoogleCalendarEventListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleCalendarClient {

    private static final String GOOGLE_CALENDAR_EVENTS_URL =
            "https://www.googleapis.com/calendar/v3/calendars/primary/events";

    private final RestTemplate restTemplate = new RestTemplate();

    public GoogleCalendarEventListResponse getCalendarEvents(String accessToken,String timeMin,String timeMax) {
        return getCalendarEventsWithPagination(accessToken, timeMin, timeMax, null);
    }

    /**
     * 페이징 처리를 통한 모든 캘린더 이벤트 조회
     */
    private GoogleCalendarEventListResponse getCalendarEventsWithPagination(String accessToken, String timeMin, String timeMax, String pageToken) {
        log.info("Google Calendar API 호출 - timeMin: {}, timeMax: {}, pageToken: {}", timeMin, timeMax, pageToken);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(GOOGLE_CALENDAR_EVENTS_URL)
                .queryParam("timeMin", timeMin)
                .queryParam("timeMax", timeMax)
                .queryParam("singleEvents", true)
                .queryParam("maxResults", 2500)
                .queryParam("orderBy", "startTime")
                .queryParam("showDeleted", false)
                .queryParam("showHiddenInvitations", false);

        // 페이지 토큰이 있으면 추가
        if (pageToken != null && !pageToken.isEmpty()) {
            uriBuilder.queryParam("pageToken", pageToken);
        }

        String url = uriBuilder.toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Object> entity = new HttpEntity<>(headers);

        ResponseEntity<GoogleCalendarEventListResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                GoogleCalendarEventListResponse.class
        );

        if(response.getStatusCode() == HttpStatus.OK) {
            GoogleCalendarEventListResponse responseBody = response.getBody();

            if (responseBody != null) {
                List<GoogleCalendarEventDto> allEvents = responseBody.getItems() != null ?
                    new java.util.ArrayList<>(responseBody.getItems()) : new java.util.ArrayList<>();

                log.info("현재 페이지에서 {} 개 이벤트 조회됨", allEvents.size());

                // 다음 페이지가 있으면 재귀적으로 호출하여 모든 데이터 수집
                if (responseBody.getNextPageToken() != null && !responseBody.getNextPageToken().isEmpty()) {
                    log.info("다음 페이지 존재, 추가 데이터 조회 중...");
                    GoogleCalendarEventListResponse nextPageResponse = getCalendarEventsWithPagination(
                        accessToken, timeMin, timeMax, responseBody.getNextPageToken()
                    );

                    if (nextPageResponse.getItems() != null) {
                        allEvents.addAll(nextPageResponse.getItems());
                    }
                }

                // 최종 결과 설정
                responseBody.setItems(allEvents);
                responseBody.setNextPageToken(null); // 모든 페이지를 합쳤으므로 토큰 제거

                log.info("전체 {} 개 이벤트 조회 완료", allEvents.size());

                return responseBody;
            } else {
                log.warn("Google Calendar API 응답이 비어있음");
                return new GoogleCalendarEventListResponse();
            }
        }

        throw new RuntimeException("Failed to fetch events from Google Calendar.");
    }

    /**
     * Google Calendar에 새 이벤트 생성
     */
    public CalendarEventResponse createEvent(String accessToken, CalendarEventCreateRequest request) {
        String url = GOOGLE_CALENDAR_EVENTS_URL;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, Object> eventData = createEventRequestBody(request);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(eventData, headers);

        log.info("Creating calendar event: {}", request.getSummary());

        ResponseEntity<CalendarEventResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                CalendarEventResponse.class
        );

        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
            return response.getBody();
        }

        throw new RuntimeException("Failed to create event in Google Calendar.");
    }

    /**
     * Google Calendar 이벤트 수정
     */
    public CalendarEventResponse updateEvent(String accessToken, String eventId, CalendarEventUpdateRequest request) {
        String url = GOOGLE_CALENDAR_EVENTS_URL + "/" + eventId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, Object> eventData = createEventUpdateRequestBody(request);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(eventData, headers);

        log.info("Updating calendar event: {} with ID: {}", request.getSummary(), eventId);

        ResponseEntity<CalendarEventResponse> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                entity,
                CalendarEventResponse.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }

        throw new RuntimeException("Failed to update event in Google Calendar.");
    }

    /**
     * Google Calendar 이벤트 삭제
     */
    public void deleteEvent(String accessToken, String eventId) {
        String url = GOOGLE_CALENDAR_EVENTS_URL + "/" + eventId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Object> entity = new HttpEntity<>(headers);

        log.info("Deleting calendar event with ID: {}", eventId);

        ResponseEntity<Void> response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                entity,
                Void.class
        );

        if (response.getStatusCode() != HttpStatus.NO_CONTENT && response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to delete event from Google Calendar.");
        }
    }

    /**
     * 이벤트 생성 요청 본문 생성
     */
    private Map<String, Object> createEventRequestBody(CalendarEventCreateRequest request) {
        Map<String, Object> event = new HashMap<>();
        event.put("summary", request.getSummary());

        if (request.getDescription() != null) {
            event.put("description", request.getDescription());
        }

        if (request.getLocation() != null) {
            event.put("location", request.getLocation());
        }

        // 시작 시간 설정
        Map<String, Object> start = new HashMap<>();
        start.put("dateTime", request.getStartDateTime());
        start.put("timeZone", request.getTimeZone());
        event.put("start", start);

        // 종료 시간 설정
        Map<String, Object> end = new HashMap<>();
        end.put("dateTime", request.getEndDateTime());
        end.put("timeZone", request.getTimeZone());
        event.put("end", end);

        return event;
    }

    /**
     * 이벤트 수정 요청 본문 생성
     */
    private Map<String, Object> createEventUpdateRequestBody(CalendarEventUpdateRequest request) {
        Map<String, Object> event = new HashMap<>();

        if (request.getSummary() != null) {
            event.put("summary", request.getSummary());
        }

        if (request.getDescription() != null) {
            event.put("description", request.getDescription());
        }

        if (request.getLocation() != null) {
            event.put("location", request.getLocation());
        }

        // 시작 시간 설정 (제공된 경우만)
        if (request.getStartDateTime() != null) {
            Map<String, Object> start = new HashMap<>();
            start.put("dateTime", request.getStartDateTime());
            start.put("timeZone", request.getTimeZone());
            event.put("start", start);
        }

        // 종료 시간 설정 (제공된 경우만)
        if (request.getEndDateTime() != null) {
            Map<String, Object> end = new HashMap<>();
            end.put("dateTime", request.getEndDateTime());
            end.put("timeZone", request.getTimeZone());
            event.put("end", end);
        }

        return event;
    }
}
