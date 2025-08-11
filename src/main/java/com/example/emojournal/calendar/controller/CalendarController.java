package com.example.emojournal.calendar.controller;

import com.example.emojournal.auth.jwt.utils.AuthenticationContextHolder;
import com.example.emojournal.calendar.dto.GoogleCalendarEventListResponse;
import com.example.emojournal.calendar.service.GoogleCalendarService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CalendarController {

    private final GoogleCalendarService googleCalendarService;

    @GetMapping("/calendar")
    public GoogleCalendarEventListResponse getCalendar(HttpServletRequest request, @RequestParam String timeMin,@RequestParam String timeMax) throws Exception {

        Long memberId = AuthenticationContextHolder.getContext();

        return googleCalendarService.fetchCalendar(memberId, timeMin, timeMax);

    }

}
