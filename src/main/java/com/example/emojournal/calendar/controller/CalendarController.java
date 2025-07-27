package com.example.emojournal.calendar.controller;

import com.example.emojournal.calendar.dto.*;
import com.example.emojournal.calendar.service.GoogleCalendarService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calendar") // 공통 URL Prefix
public class CalendarController {

    private final GoogleCalendarService googleCalendarService;

    // 일정 목록 조회 (기간 필터 포함)
    @GetMapping
    public ResponseEntity<GoogleCalendarEventListResponse> getCalendar(
            HttpServletRequest request,
            @RequestParam String timeMin,
            @RequestParam String timeMax) {
        try {
            // 인증 필터에서 설정된 사용자 ID 추출
            Long memberId = (Long) request.getAttribute("memberId");

            // Google Calendar에서 일정 조회
            GoogleCalendarEventListResponse response = googleCalendarService.fetchCalendar(memberId, timeMin, timeMax);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching calendar events", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 일정 생성
    @PostMapping("/events")
    public ResponseEntity<?> createEvent(
            HttpServletRequest request,
            @Valid @RequestBody CalendarEventCreateRequest createRequest) {
        try {
            Long memberId = (Long) request.getAttribute("memberId");
            CalendarEventResponse response = googleCalendarService.createEvent(memberId, createRequest);

            // 생성 성공 시 JSON 응답
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "message", "일정이 성공적으로 추가되었습니다.",
                            "event", response
                    ));
        } catch (Exception e) {
            log.error("Error creating calendar event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "일정 추가 중 오류가 발생했습니다.",
                            "error", e.getMessage()
                    ));
        }
    }

    // 일정 수정
    @PutMapping("/events/{eventId}")
    public ResponseEntity<?> updateEvent(
            HttpServletRequest request,
            @PathVariable String eventId,
            @Valid @RequestBody CalendarEventUpdateRequest updateRequest) {
        try {
            Long memberId = (Long) request.getAttribute("memberId");
            CalendarEventResponse response = googleCalendarService.updateEvent(memberId, eventId, updateRequest);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "일정이 성공적으로 수정되었습니다.",
                    "event", response
            ));
        } catch (Exception e) {
            log.error("Error updating calendar event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "일정 수정 중 오류가 발생했습니다.",
                            "error", e.getMessage()
                    ));
        }
    }

    // 일정 삭제
    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<?> deleteEvent(
            HttpServletRequest request,
            @PathVariable String eventId) {
        try {
            Long memberId = (Long) request.getAttribute("memberId");
            googleCalendarService.deleteEvent(memberId, eventId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "일정이 성공적으로 삭제되었습니다."
            ));
        } catch (Exception e) {
            log.error("Error deleting calendar event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "일정 삭제 중 오류가 발생했습니다.",
                            "error", e.getMessage()
                    ));
        }
    }

    // 단일 일정 상세 조회
    @GetMapping("/events/{eventId}")
    public ResponseEntity<?> getEvent(
            HttpServletRequest request,
            @PathVariable String eventId) {
        try {
            Long memberId = (Long) request.getAttribute("memberId");
            CalendarEventResponse response = googleCalendarService.getEvent(memberId, eventId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "event", response
            ));
        } catch (Exception e) {
            log.error("Error fetching calendar event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "일정 조회 중 오류가 발생했습니다.",
                            "error", e.getMessage()
                    ));
        }
    }
}
