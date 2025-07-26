package com.example.emojournal.calendar.service;

import com.example.emojournal.calendar.client.GoogleCalendarClient;
import com.example.emojournal.calendar.dto.*;
import com.example.emojournal.auth.jwt.utils.crypto.CryptoUtil;
import com.example.emojournal.auth.oauth.entity.GoogleToken;
import com.example.emojournal.auth.oauth.client.GoogleApiClient;
import com.example.emojournal.auth.oauth.dto.response.GoogleAccessTokenResponse;
import com.example.emojournal.member.entity.Member;
import com.example.emojournal.auth.oauth.repository.GoogleTokenRepository;
import com.example.emojournal.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GoogleCalendarService {

    private final GoogleCalendarClient googleCalendarClient;
    private final GoogleTokenRepository googleTokenRepository;
    private final MemberRepository memberRepository;
    private final CryptoUtil cryptoUtil;
    private final GoogleApiClient googleApiClient;


    public GoogleCalendarEventListResponse fetchCalendar(Long memberId, String timeMin, String timeMax) throws Exception {
        String accessToken = getValidAccessToken(memberId);
        return googleCalendarClient.getCalendarEvents(accessToken, toUtcString(timeMin), toUtcString(timeMax));
    }



    // 일정 추가
    public CalendarEventResponse createEvent(Long memberId, CalendarEventCreateRequest request) throws Exception {
        String accessToken = getValidAccessToken(memberId);

        GoogleCalendarEventCreateRequest googleRequest = convertToGoogleRequest(request);
        GoogleCalendarEventDto createdEvent = googleCalendarClient.createEvent(accessToken, googleRequest);

        return convertToResponse(createdEvent);
    }

    // 일정 수정
    public CalendarEventResponse updateEvent(Long memberId, String eventId, CalendarEventUpdateRequest request) throws Exception {
        String accessToken = getValidAccessToken(memberId);

        GoogleCalendarEventCreateRequest googleRequest = convertToGoogleRequest(request);
        GoogleCalendarEventDto updatedEvent = googleCalendarClient.updateEvent(accessToken, eventId, googleRequest);

        return convertToResponse(updatedEvent);
    }

    // 일정 삭제
    public void deleteEvent(Long memberId, String eventId) throws Exception {
        String accessToken = getValidAccessToken(memberId);
        googleCalendarClient.deleteEvent(accessToken, eventId);
    }

    // 단일 일정 조회
    public CalendarEventResponse getEvent(Long memberId, String eventId) throws Exception {
        String accessToken = getValidAccessToken(memberId);
        GoogleCalendarEventDto event = googleCalendarClient.getEvent(accessToken, eventId);
        return convertToResponse(event);
    }

    // Access Token 유효성 검사 및 갱신
    private String getValidAccessToken(Long memberId) throws Exception {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("Member not found"));

        GoogleToken googleToken = googleTokenRepository.findByMember(member)
                .orElseThrow(() -> new NoSuchElementException("Google token not found"));

        // 토큰 만료 검사 (1분 여유시간 둠)
        if (googleToken.getAccessTokenExpiresAt().isBefore(LocalDateTime.now().plusMinutes(1))) {
            log.info("Access token expired, refreshing...");
            refreshAccessToken(googleToken);
        }

        return cryptoUtil.decrypt(googleToken.getAccessToken());
    }

    // Access Token 갱신
    private void refreshAccessToken(GoogleToken googleToken) throws Exception {
        String refreshToken = cryptoUtil.decrypt(googleToken.getRefreshToken());

        GoogleAccessTokenResponse newTokenResponse = googleApiClient.refreshAccessToken(refreshToken);

        // 새로운 토큰으로 업데이트
        googleToken.updateAccessToken(
                cryptoUtil.encrypt(newTokenResponse.getAccessToken()),
                LocalDateTime.now().plusSeconds(newTokenResponse.getExpiresIn())
        );

        googleTokenRepository.save(googleToken);
        log.info("Access token refreshed successfully");
    }

    // DTO 변환 메서드들
    private GoogleCalendarEventCreateRequest convertToGoogleRequest(CalendarEventCreateRequest request) {
        GoogleCalendarEventCreateRequest.EventDateTime start, end;

        if (request.isAllDay()) {
            // 종일 일정
            String startDate = request.getStartDateTime().substring(0, 10); // "2025-04-15"
            String endDate = request.getEndDateTime().substring(0, 10);

            start = GoogleCalendarEventCreateRequest.EventDateTime.builder()
                    .date(startDate)
                    .build();
            end = GoogleCalendarEventCreateRequest.EventDateTime.builder()
                    .date(endDate)
                    .build();
        } else {
            // 시간 지정 일정
            start = GoogleCalendarEventCreateRequest.EventDateTime.builder()
                    .dateTime(request.getStartDateTime())
                    .timeZone("Asia/Seoul")
                    .build();
            end = GoogleCalendarEventCreateRequest.EventDateTime.builder()
                    .dateTime(request.getEndDateTime())
                    .timeZone("Asia/Seoul")
                    .build();
        }

        return GoogleCalendarEventCreateRequest.builder()
                .summary(request.getSummary())
                .description(request.getDescription())
                .location(request.getLocation())
                .start(start)
                .end(end)
                .build();
    }

    private GoogleCalendarEventCreateRequest convertToGoogleRequest(CalendarEventUpdateRequest request) {
        GoogleCalendarEventCreateRequest.EventDateTime start, end;

        if (request.isAllDay()) {
            String startDate = request.getStartDateTime().substring(0, 10);
            String endDate = request.getEndDateTime().substring(0, 10);

            start = GoogleCalendarEventCreateRequest.EventDateTime.builder()
                    .date(startDate)
                    .build();
            end = GoogleCalendarEventCreateRequest.EventDateTime.builder()
                    .date(endDate)
                    .build();
        } else {
            start = GoogleCalendarEventCreateRequest.EventDateTime.builder()
                    .dateTime(request.getStartDateTime())
                    .timeZone("Asia/Seoul")
                    .build();
            end = GoogleCalendarEventCreateRequest.EventDateTime.builder()
                    .dateTime(request.getEndDateTime())
                    .timeZone("Asia/Seoul")
                    .build();
        }

        return GoogleCalendarEventCreateRequest.builder()
                .summary(request.getSummary())
                .description(request.getDescription())
                .location(request.getLocation())
                .start(start)
                .end(end)
                .build();
    }

    private CalendarEventResponse convertToResponse(GoogleCalendarEventDto dto) {
        boolean allDay = false;
        String startDateTime = null;
        String endDateTime = null;

        if (dto.getStart() != null) {
            if (dto.getStart().getDate() != null) {
                // 종일 일정
                allDay = true;
                startDateTime = dto.getStart().getDate();
            } else if (dto.getStart().getDateTime() != null) {
                // 시간 지정 일정
                startDateTime = dto.getStart().getDateTime();
            }
        }

        if (dto.getEnd() != null) {
            if (dto.getEnd().getDate() != null) {
                endDateTime = dto.getEnd().getDate();
            } else if (dto.getEnd().getDateTime() != null) {
                endDateTime = dto.getEnd().getDateTime();
            }
        }

        return CalendarEventResponse.builder()
                .id(dto.getId())
                .summary(dto.getSummary())
                .description(dto.getDescription())
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .allDay(allDay)
                .status("confirmed")
                .build();
    }

    // ✅ 기존 메서드 - 그대로 유지
    private String toUtcString(String time) {
        LocalDateTime localDateTime = LocalDateTime.parse(time);
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("Asia/Seoul"));
        Instant utcInstant = zonedDateTime.toInstant();
        return utcInstant.toString();
    }
}