package com.example.emojournal.calendar.controller;

import com.example.emojournal.auth.jwt.utils.AuthenticationContextHolder;
import com.example.emojournal.calendar.dto.CalendarEventCreateRequest;
import com.example.emojournal.calendar.dto.CalendarEventResponse;
import com.example.emojournal.calendar.dto.CalendarEventUpdateRequest;
import com.example.emojournal.calendar.dto.GoogleCalendarEventDto;
import com.example.emojournal.calendar.dto.GoogleCalendarEventListResponse;
import com.example.emojournal.calendar.service.GoogleCalendarService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CalendarController {

    private final GoogleCalendarService googleCalendarService;

    @GetMapping("/calendar")
    public List<GoogleCalendarEventDto> getCalendar(HttpServletRequest request, @RequestParam String timeMin,@RequestParam String timeMax) throws Exception {

        Long memberId = AuthenticationContextHolder.getContext();

        GoogleCalendarEventListResponse response = googleCalendarService.fetchCalendar(memberId, timeMin, timeMax);

        // items 배열을 직접 반환 (null 체크 포함)
        return response.getItems() != null ? response.getItems() : List.of();

    }

    /**
     * 캘린더 이벤트 생성
     */
    @PostMapping("/calendar/events")
    public ResponseEntity<CalendarEventResponse> createCalendarEvent(
            HttpServletRequest request,
            @RequestBody CalendarEventCreateRequest createRequest) throws Exception {

        Long memberId = AuthenticationContextHolder.getContext();

        CalendarEventResponse response = googleCalendarService.createCalendarEvent(memberId, createRequest);

        return ResponseEntity.ok(response);
    }

    /**
     * 캘린더 이벤트 수정
     */
    @PutMapping("/calendar/events/{eventId}")
    public ResponseEntity<CalendarEventResponse> updateCalendarEvent(
            HttpServletRequest request,
            @PathVariable String eventId,
            @RequestBody CalendarEventUpdateRequest updateRequest) throws Exception {

        Long memberId = AuthenticationContextHolder.getContext();

        CalendarEventResponse response = googleCalendarService.updateCalendarEvent(memberId, eventId, updateRequest);

        return ResponseEntity.ok(response);
    }

    /**
     * 캘린더 이벤트 삭제
     */
    @DeleteMapping("/calendar/events/{eventId}")
    public ResponseEntity<Map<String, String>> deleteCalendarEvent(
            HttpServletRequest request,
            @PathVariable String eventId) throws Exception {

        Long memberId = AuthenticationContextHolder.getContext();

        googleCalendarService.deleteCalendarEvent(memberId, eventId);

        return ResponseEntity.ok(Map.of("message", "이벤트가 성공적으로 삭제되었습니다."));
    }

}
