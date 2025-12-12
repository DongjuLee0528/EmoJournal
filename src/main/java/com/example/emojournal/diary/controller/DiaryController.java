package com.example.emojournal.diary.controller;

import com.example.emojournal.diary.dto.DiaryCreateRequest;
import com.example.emojournal.diary.dto.DiaryResponse;
import com.example.emojournal.diary.dto.DiaryUpdateRequest;
import com.example.emojournal.diary.service.DiaryService;
import com.example.emojournal.diary.service.FileUploadService;
import com.example.emojournal.auth.jwt.utils.AuthenticationContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 일기 관리 REST API 컨트롤러
 *
 * 사용자의 일기 작성, 수정, 삭제, 조회 및 감정 분석 기능을 제공하는 REST API 엔드포인트를
 * 담당합니다. JWT 인증을 통해 사용자 식별 및 권한 검증을 수행합니다.
 *
 * 주요 기능:
 * - 일기 생성 (텍스트 + 이미지 첨부 지원)
 * - 일기 수정 및 삭제
 * - 일기 목록 조회 (페이징 지원)
 * - 일기 검색 및 감정별 필터링
 * - 감정 통계 조회
 * - 1년 전 오늘의 일기 조회
 *
 * @author EmoJournal Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    /** 일기 비즈니스 로직을 처리하는 서비스 */
    private final DiaryService diaryService;
    /** 파일 업로드 관련 서비스 */
    private final FileUploadService fileUploadService;

    /**
     * JWT 인증 컨텍스트에서 사용자 ID를 추출합니다.
     * AuthenticationContextHolder에 저장된 memberId를 기반으로 userId를 생성합니다.
     *
     * @return 생성된 사용자 ID ("member_" + memberId)
     * @throws SecurityException 인증되지 않은 사용자인 경우
     */
    private String getUserIdFromContext() {
        Long memberId = AuthenticationContextHolder.getContext();
        log.debug("[AUTH] AuthenticationContextHolder에서 추출한 memberId: {}", memberId);

        if (memberId == null) {
            log.error("[AUTH] 인증 실패 - AuthenticationContextHolder에 memberId가 없음");
            throw new SecurityException("인증되지 않은 사용자입니다.");
        }

        String userId = "member_" + memberId;
        log.debug("[AUTH] userId 생성 완료: {}", userId);
        return userId;
    }

    /**
     * 이미지를 포함한 일기를 생성합니다.
     * Multipart form-data 형식으로 텍스트와 이미지를 함께 전송받아 처리합니다.
     *
     * @param request 일기 생성 요청 데이터 (제목, 내용, 날짜, 공개여부)
     * @param imageFile 첨부할 이미지 파일 (선택사항)
     * @param httpRequest HTTP 요청 객체
     * @return 생성된 일기 정보와 감정 분석 결과
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DiaryResponse> createDiary(
            @Valid @ModelAttribute DiaryCreateRequest request,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            HttpServletRequest httpRequest) {

        log.info("[DIARY_CREATE_MULTIPART] API 진입 - URI: {}, Method: {}", httpRequest.getRequestURI(), httpRequest.getMethod());

        try {
            String userId = getUserIdFromContext();
            request.setUserId(userId);

            // 상세 요청 정보 로깅
            log.info("[DIARY_CREATE_MULTIPART] 요청 상세 - 사용자: {}, 제목: [{}], 내용길이: {}, 작성날짜: {}, 공개여부: {}",
                    userId, request.getTitle(),
                    request.getContent() != null ? request.getContent().length() : 0,
                    request.getDiaryDate(), request.getIsPublic());

            // 파일 정보 로깅
            if (imageFile != null && !imageFile.isEmpty()) {
                log.info("[DIARY_CREATE_MULTIPART] 이미지 파일 - 원본명: [{}], 크기: {}bytes, ContentType: {}",
                        imageFile.getOriginalFilename(), imageFile.getSize(), imageFile.getContentType());
            } else {
                log.info("[DIARY_CREATE_MULTIPART] 이미지 파일 없음");
            }

            DiaryResponse response = diaryService.createDiary(request, imageFile);

            log.info("[DIARY_CREATE_MULTIPART] 일기 생성 완료 - 일기 ID: {}, 감정: {}", response.getId(), response.getEmotionKeyword());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("[DIARY_CREATE_MULTIPART] 유효성 검증 실패 - 사용자: {}, 오류: {}", request.getUserId(), e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.error("[DIARY_CREATE_MULTIPART] 인증/권한 오류 - 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[DIARY_CREATE_MULTIPART] 일기 생성 실패 - 사용자: {}, 오류: {}", request.getUserId(), e.getMessage(), e);
            throw new RuntimeException("일기 생성에 실패했습니다", e);
        }
    }

    /**
     * 텍스트만으로 간단한 일기를 생성합니다.
     * JSON 형식으로 텍스트 데이터만 전송받아 처리합니다.
     *
     * @param request 일기 생성 요청 데이터
     * @param httpRequest HTTP 요청 객체
     * @return 생성된 일기 정보와 감정 분석 결과
     */
    @PostMapping("/simple")
    public ResponseEntity<DiaryResponse> createSimpleDiary(
            @Valid @RequestBody DiaryCreateRequest request,
            HttpServletRequest httpRequest) {

        log.info("[DIARY_CREATE_SIMPLE] API 진입 - URI: {}, Method: {}", httpRequest.getRequestURI(), httpRequest.getMethod());

        try {
            String userId = getUserIdFromContext();
            request.setUserId(userId);

            // 상세 요청 정보 로깅
            log.info("[DIARY_CREATE_SIMPLE] 요청 상세 - 사용자: {}, 제목: [{}], 내용길이: {}, 작성날짜: {}, 공개여부: {}",
                    userId, request.getTitle(),
                    request.getContent() != null ? request.getContent().length() : 0,
                    request.getDiaryDate(), request.getIsPublic());

            DiaryResponse response = diaryService.createDiary(request, null);

            log.info("[DIARY_CREATE_SIMPLE] 일기 생성 완료 - 일기 ID: {}, 감정: {}", response.getId(), response.getEmotionKeyword());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("[DIARY_CREATE_SIMPLE] 유효성 검증 실패 - 사용자: {}, 오류: {}", request.getUserId(), e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.error("[DIARY_CREATE_SIMPLE] 인증/권한 오류 - 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[DIARY_CREATE_SIMPLE] 일기 생성 실패 - 사용자: {}, 오류: {}", request.getUserId(), e.getMessage(), e);
            throw new RuntimeException("일기 생성에 실패했습니다", e);
        }
    }

    /**
     * 기존 일기를 수정합니다.
     * 텍스트 내용과 이미지를 함께 수정할 수 있으며, 감정 재분석도 요청할 수 있습니다.
     *
     * @param id 수정할 일기 ID
     * @param request 일기 수정 요청 데이터
     * @param imageFile 새로 첨부할 이미지 파일 (선택사항)
     * @param httpRequest HTTP 요청 객체
     * @return 수정된 일기 정보
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DiaryResponse> updateDiary(
            @PathVariable Long id,
            @Valid @ModelAttribute DiaryUpdateRequest request,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            HttpServletRequest httpRequest) {

        String userId = getUserIdFromContext();
        return ResponseEntity.ok(diaryService.updateDiary(id, userId, request, imageFile));
    }

    /**
     * 특정 일기를 조회합니다.
     * 조회 시 해당 일기의 조회수가 1 증가합니다.
     *
     * @param id 조회할 일기 ID
     * @param httpRequest HTTP 요청 객체
     * @return 일기 상세 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<DiaryResponse> getDiary(@PathVariable Long id, HttpServletRequest httpRequest) {
        String userId = getUserIdFromContext();
        return ResponseEntity.ok(diaryService.getDiary(id, userId));
    }

    /**
     * 사용자의 일기 목록을 페이징하여 조회합니다.
     * 최신 일기가 먼저 나오도록 날짜 내림차순으로 정렬됩니다.
     *
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @param httpRequest HTTP 요청 객체
     * @return 페이징된 일기 목록
     */
    @GetMapping
    public ResponseEntity<Page<DiaryResponse>> getDiaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {

        String userId = getUserIdFromContext();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "diaryDate"));
        return ResponseEntity.ok(diaryService.getDiaries(userId, pageable));
    }

    /**
     * 사용자의 모든 일기를 조회합니다.
     * 페이징 없이 전체 일기를 반환하므로 대용량 데이터 시 주의가 필요합니다.
     *
     * @param request HTTP 요청 객체
     * @return 전체 일기 목록
     */
    @GetMapping("/all")
    public ResponseEntity<List<DiaryResponse>> getAllDiaries(HttpServletRequest request) {
        String userId = getUserIdFromContext();
        return ResponseEntity.ok(diaryService.getAllDiaries(userId));
    }

    /**
     * 1년 전 오늘 작성한 일기를 조회합니다.
     * 추억을 되돌아볼 수 있는 기능을 제공합니다.
     *
     * @param request HTTP 요청 객체
     * @return 1년 전 오늘의 일기 (없으면 null)
     */
    @GetMapping("/one-year-ago")
    public ResponseEntity<DiaryResponse> getOneYearAgoDiary(HttpServletRequest request) {
        String userId = getUserIdFromContext();
        return ResponseEntity.ok(diaryService.getOneYearAgoDiary(userId));
    }

    /**
     * 특정 일기를 삭제합니다.
     * 첨부된 이미지 파일도 함께 삭제됩니다.
     *
     * @param id 삭제할 일기 ID
     * @param request HTTP 요청 객체
     * @return 삭제 완료 응답
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiary(@PathVariable Long id, HttpServletRequest request) {
        String userId = getUserIdFromContext();
        diaryService.deleteDiary(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 키워드로 일기를 검색합니다.
     * 제목과 내용에서 키워드를 포함하는 일기를 검색합니다.
     *
     * @param keyword 검색할 키워드
     * @param request HTTP 요청 객체
     * @return 검색된 일기 목록
     */
    @GetMapping("/search")
    public ResponseEntity<List<DiaryResponse>> searchDiaries(
            @RequestParam String keyword,
            HttpServletRequest request) {
        String userId = getUserIdFromContext();
        return ResponseEntity.ok(diaryService.searchDiaries(userId, keyword));
    }

    /**
     * 특정 감정으로 분류된 일기들을 조회합니다.
     * 감정 분석 결과를 기반으로 필터링합니다.
     *
     * @param emotion 검색할 감정 (예: "기쁨", "슬픔", "분노" 등)
     * @param request HTTP 요청 객체
     * @return 해당 감정의 일기 목록
     */
    @GetMapping("/emotion/{emotion}")
    public ResponseEntity<List<DiaryResponse>> getDiariesByEmotion(
            @PathVariable String emotion,
            HttpServletRequest request) {
        String userId = getUserIdFromContext();
        return ResponseEntity.ok(diaryService.getDiariesByEmotion(userId, emotion));
    }

    /**
     * 사용자의 감정 통계를 조회합니다.
     * 각 감정별로 작성된 일기의 수를 통계로 제공합니다.
     *
     * @param request HTTP 요청 객체
     * @return 감정별 통계 정보와 총 일기 수
     */
    @GetMapping("/statistics/emotion")
    public ResponseEntity<Map<String, Object>> getEmotionStatistics(HttpServletRequest request) {
        String userId = getUserIdFromContext();
        Map<String, Long> stats = diaryService.getEmotionStatistics(userId);
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "statistics", stats,
                "totalDiaries", stats.values().stream().mapToLong(Long::longValue).sum()
        ));
    }

    /**
     * 파일 업로드 관련 설정 정보를 조회합니다.
     * 클라이언트에서 파일 업로드 시 참고할 수 있는 제한사항을 제공합니다.
     *
     * @return 파일 크기 제한, 허용 확장자 등의 업로드 설정 정보
     */
    @GetMapping("/upload-info")
    public ResponseEntity<Map<String, Object>> getUploadInfo() {
        return ResponseEntity.ok(Map.of(
                "maxFileSize", fileUploadService.getMaxFileSize(),
                "maxFileSizeReadable", fileUploadService.getMaxFileSizeReadable(),
                "allowedExtensions", fileUploadService.getAllowedExtensions(),
                "uploadPath", fileUploadService.getUploadPath()
        ));
    }

    /**
     * 일기 API 서버의 상태를 확인합니다.
     * 시스템 모니터링 및 헬스체크용 엔드포인트입니다.
     *
     * @return 서버 상태 정보
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "message", "Diary API is running with JWT Authentication!",
                "version", "1.0"
        ));
    }
}
