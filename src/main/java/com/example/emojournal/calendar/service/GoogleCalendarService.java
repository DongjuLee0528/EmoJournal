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

/**
 * Google Calendar API 연동 서비스
 *
 * Google Calendar API를 통해 사용자의 캘린더 이벤트를 관리하는 서비스입니다.
 * 사용자의 Google OAuth 토큰을 사용하여 캘린더 데이터에 안전하게 접근하고,
 * 이벤트의 생성, 조회, 수정, 삭제 기능을 제공합니다.
 *
 * 주요 기능:
 * - 지정된 기간의 캘린더 이벤트 조회
 * - 새로운 캘린더 이벤트 생성
 * - 기존 이벤트 수정 및 삭제
 * - UTC 시간 변환 및 처리
 * - 사용자 인증 토큰 관리
 * - 암호화된 토큰 복호화 지원
 *
 * 보안 주의사항:
 * - Google OAuth 토큰은 암호화되어 데이터베이스에 저장
 * - API 호출 시 토큰을 복호화하여 사용
 */
@Service
@RequiredArgsConstructor
public class GoogleCalendarService {
    /** Google Calendar API 호출을 담당하는 클라이언트 */
    private final GoogleCalendarClient googleCalendarClient;

    /** Google OAuth 토큰 정보를 저장하는 리포지토리 */
    private final GoogleTokenRepository googleTokenRepository;

    /** 회원 정보를 관리하는 리포지토리 */
    private final MemberRepository memberRepository;

    /** 토큰 암호화/복호화를 담당하는 유틸리티 */
    private final CryptoUtil cryptoUtil;


    /**
     * 지정된 기간의 캘린더 이벤트를 조회합니다.
     *
     * Google Calendar API를 통해 사용자의 캘린더에서 지정된 기간 내의
     * 모든 이벤트를 가져옵니다. 시간은 UTC로 변환하여 처리합니다.
     *
     * @param memberId 캘린더 이벤트를 조회할 회원의 ID
     * @param timeMin 조회 시작 시간 (ISO 8601 형식)
     * @param timeMax 조회 종료 시간 (ISO 8601 형식)
     * @return Google Calendar 이벤트 목록 응답
     * @throws Exception 인증 실패, 토큰 만료, API 호출 오류 시
     */
    public GoogleCalendarEventListResponse fetchCalendar(Long memberId,String timeMin,String timeMax) throws Exception {
        String accessToken = getAccessToken(memberId);
        return googleCalendarClient.getCalendarEvents(accessToken, toUtcString(timeMin), toUtcString(timeMax));
    }

    /**
     * ISO 8601 날짜 문자열을 UTC 시간 문자열로 변환합니다.
     *
     * 입력받은 날짜 문자열을 파싱하여 UTC 타임스탬프로 변환합니다.
     * Google Calendar API는 UTC 시간을 요구하므로 시간대 변환이 필요합니다.
     *
     * @param time ISO 8601 형식의 날짜 문자열 (e.g., 2025-07-31T15:00:00.000Z)
     * @return UTC 형식의 날짜 문자열
     * @throws java.time.format.DateTimeParseException 날짜 형식이 잘못된 경우
     */
    private String toUtcString(String time) {
        // ISO 8601 형식의 날짜 문자열을 OffsetDateTime으로 파싱
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(time);

        // UTC Instant로 변환하여 시간대 정보 제거
        Instant utcInstant = offsetDateTime.toInstant();

        return utcInstant.toString();
    }

    /**
     * 새로운 캘린더 이벤트를 생성합니다.
     *
     * 사용자의 Google Calendar에 새로운 이벤트를 추가합니다.
     * 이벤트 제목, 설명, 시간, 참석자 등의 정보를 설정할 수 있습니다.
     *
     * @param memberId 이벤트를 생성할 회원의 ID
     * @param request 생성할 이벤트의 상세 정보
     * @return 생성된 이벤트 정보
     * @throws Exception 인증 실패, 토큰 만료, API 호출 오류 또는 권한 부족 시
     */
    public CalendarEventResponse createCalendarEvent(Long memberId, CalendarEventCreateRequest request) throws Exception {
        String accessToken = getAccessToken(memberId);
        return googleCalendarClient.createEvent(accessToken, request);
    }

    /**
     * 기존 캘린더 이벤트를 수정합니다.
     *
     * 지정된 이벤트 ID를 가진 기존 이벤트의 정보를 업데이트합니다.
     * 제목, 시간, 설명 등의 정보를 변경할 수 있습니다.
     *
     * @param memberId 이벤트를 수정할 회원의 ID
     * @param eventId 수정할 캘린더 이벤트의 고유 ID
     * @param request 수정할 이벤트 정보
     * @return 수정된 이벤트 정보
     * @throws Exception 인증 실패, 이벤트 찾기 실패, API 호출 오류 또는 권한 부족 시
     */
    public CalendarEventResponse updateCalendarEvent(Long memberId, String eventId, CalendarEventUpdateRequest request) throws Exception {
        String accessToken = getAccessToken(memberId);
        return googleCalendarClient.updateEvent(accessToken, eventId, request);
    }

    /**
     * 캘린더 이벤트를 삭제합니다.
     *
     * 지정된 이벤트 ID를 가진 이벤트를 사용자의 Google Calendar에서 완전히 삭제합니다.
     * 삭제된 이벤트는 복구할 수 없으므로 주의가 필요합니다.
     *
     * @param memberId 이벤트를 삭제할 회원의 ID
     * @param eventId 삭제할 캘린더 이벤트의 고유 ID
     * @throws Exception 인증 실패, 이벤트 찾기 실패, API 호출 오류 또는 권한 부족 시
     */
    public void deleteCalendarEvent(Long memberId, String eventId) throws Exception {
        String accessToken = getAccessToken(memberId);
        googleCalendarClient.deleteEvent(accessToken, eventId);
    }

    /**
     * 회원의 Google OAuth 액세스 토큰을 안전하게 가져옵니다.
     *
     * 데이터베이스에 암호화되어 저장된 Google OAuth 액세스 토큰을
     * 찾아서 복호화한 후 반환합닄다. Google Calendar API 호출 시 필요합니다.
     *
     * @param memberId Google Calendar에 접근할 회원의 ID
     * @return 복호화된 Google OAuth 액세스 토큰
     * @throws NoSuchElementException 회원을 찾을 수 없거나 Google 토큰이 없는 경우
     * @throws Exception 토큰 복호화 실패 시
     */
    private String getAccessToken(Long memberId) throws Exception {
        Member member = memberRepository.findById(memberId).orElseThrow(NoSuchElementException::new);
        GoogleToken googleToken = googleTokenRepository.findByMemberId(member.getId()).orElseThrow(NoSuchElementException::new);
        return cryptoUtil.decrypt(googleToken.getAccessToken());
    }

}
