package com.example.emojournal.calendar.service;


import com.example.emojournal.calendar.client.GoogleCalendarClient;
import com.example.emojournal.calendar.dto.CalendarEventCreateRequest;
import com.example.emojournal.calendar.dto.CalendarEventResponse;
import com.example.emojournal.calendar.dto.CalendarEventUpdateRequest;
import com.example.emojournal.calendar.dto.GoogleCalendarEventListResponse;
import com.example.emojournal.auth.jwt.utils.crypto.CryptoUtil;
import com.example.emojournal.auth.oauth.entity.GoogleToken;
import com.example.emojournal.member.entity.Member;
import com.example.emojournal.auth.oauth.repository.GoogleTokenRepository;
import com.example.emojournal.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class GoogleCalendarService {
    private final GoogleCalendarClient googleCalendarClient;

    private final GoogleTokenRepository googleTokenRepository;

    private final MemberRepository memberRepository;

    private final CryptoUtil cryptoUtil;


    public GoogleCalendarEventListResponse fetchCalendar(Long memberId,String timeMin,String timeMax) throws Exception {
        String accessToken = getAccessToken(memberId);
        return googleCalendarClient.getCalendarEvents(accessToken, toUtcString(timeMin), toUtcString(timeMax));
    }

    private String toUtcString(String time) {
        // ISO 8601 형식 (2025-07-31T15:00:00.000Z)을 파싱
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(time);

        // UTC Instant로 변환
        Instant utcInstant = offsetDateTime.toInstant();

        return utcInstant.toString();
    }

    /**
     * 캘린더 이벤트 생성
     */
    public CalendarEventResponse createCalendarEvent(Long memberId, CalendarEventCreateRequest request) throws Exception {
        String accessToken = getAccessToken(memberId);
        return googleCalendarClient.createEvent(accessToken, request);
    }

    /**
     * 캘린더 이벤트 수정
     */
    public CalendarEventResponse updateCalendarEvent(Long memberId, String eventId, CalendarEventUpdateRequest request) throws Exception {
        String accessToken = getAccessToken(memberId);
        return googleCalendarClient.updateEvent(accessToken, eventId, request);
    }

    /**
     * 캘린더 이벤트 삭제
     */
    public void deleteCalendarEvent(Long memberId, String eventId) throws Exception {
        String accessToken = getAccessToken(memberId);
        googleCalendarClient.deleteEvent(accessToken, eventId);
    }

    /**
     * 공통 메서드: 회원의 Google 액세스 토큰 가져오기
     */
    private String getAccessToken(Long memberId) throws Exception {
        Member member = memberRepository.findById(memberId).orElseThrow(NoSuchElementException::new);
        GoogleToken googleToken = googleTokenRepository.findByMemberId(member.getId()).orElseThrow(NoSuchElementException::new);
        return cryptoUtil.decrypt(googleToken.getAccessToken());
    }

}
