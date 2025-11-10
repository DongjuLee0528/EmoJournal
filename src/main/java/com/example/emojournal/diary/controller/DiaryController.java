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

@Slf4j
@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final FileUploadService fileUploadService;

    // AuthenticationContextHolder에서 인증된 사용자 ID 추출
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

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DiaryResponse> updateDiary(
            @PathVariable Long id,
            @Valid @ModelAttribute DiaryUpdateRequest request,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            HttpServletRequest httpRequest) {

        String userId = getUserIdFromContext();
        return ResponseEntity.ok(diaryService.updateDiary(id, userId, request, imageFile));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiaryResponse> getDiary(@PathVariable Long id, HttpServletRequest httpRequest) {
        String userId = getUserIdFromContext();
        return ResponseEntity.ok(diaryService.getDiary(id, userId));
    }

    @GetMapping
    public ResponseEntity<Page<DiaryResponse>> getDiaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {

        String userId = getUserIdFromContext();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "diaryDate"));
        return ResponseEntity.ok(diaryService.getDiaries(userId, pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<List<DiaryResponse>> getAllDiaries(HttpServletRequest request) {
        String userId = getUserIdFromContext();
        return ResponseEntity.ok(diaryService.getAllDiaries(userId));
    }

    @GetMapping("/one-year-ago")
    public ResponseEntity<DiaryResponse> getOneYearAgoDiary(HttpServletRequest request) {
        String userId = getUserIdFromContext();
        return ResponseEntity.ok(diaryService.getOneYearAgoDiary(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiary(@PathVariable Long id, HttpServletRequest request) {
        String userId = getUserIdFromContext();
        diaryService.deleteDiary(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<DiaryResponse>> searchDiaries(
            @RequestParam String keyword,
            HttpServletRequest request) {
        String userId = getUserIdFromContext();
        return ResponseEntity.ok(diaryService.searchDiaries(userId, keyword));
    }

    @GetMapping("/emotion/{emotion}")
    public ResponseEntity<List<DiaryResponse>> getDiariesByEmotion(
            @PathVariable String emotion,
            HttpServletRequest request) {
        String userId = getUserIdFromContext();
        return ResponseEntity.ok(diaryService.getDiariesByEmotion(userId, emotion));
    }

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

    @GetMapping("/upload-info")
    public ResponseEntity<Map<String, Object>> getUploadInfo() {
        return ResponseEntity.ok(Map.of(
                "maxFileSize", fileUploadService.getMaxFileSize(),
                "maxFileSizeReadable", fileUploadService.getMaxFileSizeReadable(),
                "allowedExtensions", fileUploadService.getAllowedExtensions(),
                "uploadPath", fileUploadService.getUploadPath()
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "message", "Diary API is running with JWT Authentication!",
                "version", "1.0"
        ));
    }
}
