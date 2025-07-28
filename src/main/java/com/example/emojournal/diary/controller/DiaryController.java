package com.example.emojournal.diary.controller;

import com.example.emojournal.diary.dto.DiaryCreateRequest;
import com.example.emojournal.diary.dto.DiaryResponse;
import com.example.emojournal.diary.dto.DiaryUpdateRequest;
import com.example.emojournal.diary.service.DiaryService;
import com.example.emojournal.diary.service.FileUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@CrossOrigin(origins = "*")
public class DiaryController {

    private final DiaryService diaryService;
    private final FileUploadService fileUploadService;

    /**
     * 일기 생성 (텍스트 + 이미지 파일)
     * POST /api/diary
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DiaryResponse> createDiary(
            @Valid @ModelAttribute DiaryCreateRequest request,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {

        try {
            log.info("일기 생성 API 호출 - 사용자: {}, 이미지 포함: {}",
                    request.getUserId(), imageFile != null && !imageFile.isEmpty());

            DiaryResponse response = diaryService.createDiary(request, imageFile);

            log.info("일기 생성 성공 - ID: {}, 감정: {}", response.getId(), response.getAnalyzedEmotion());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("일기 생성 API 오류", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 일기 수정
     * PUT /api/diary/{id}
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DiaryResponse> updateDiary(
            @PathVariable Long id,
            @RequestParam String userId, // 추후 인증에서 자동으로 가져올 예정
            @Valid @ModelAttribute DiaryUpdateRequest request,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {

        try {
            log.info("일기 수정 API 호출 - ID: {}, 사용자: {}", id, userId);

            DiaryResponse response = diaryService.updateDiary(id, userId, request, imageFile);

            log.info("일기 수정 성공 - ID: {}", id);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("일기 수정 실패 - ID: {}, 오류: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("일기 수정 API 오류 - ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 일기 상세 조회
     * GET /api/diary/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<DiaryResponse> getDiary(
            @PathVariable Long id,
            @RequestParam String userId) {

        try {
            log.info("일기 조회 API 호출 - ID: {}, 사용자: {}", id, userId);

            DiaryResponse response = diaryService.getDiary(id, userId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("일기 조회 실패 - ID: {}, 오류: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("일기 조회 API 오류 - ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 일기 목록 조회 (페이징)
     * GET /api/diary
     */
    @GetMapping
    public ResponseEntity<Page<DiaryResponse>> getDiaries(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            log.info("일기 목록 조회 API 호출 - 사용자: {}, 페이지: {}, 크기: {}", userId, page, size);

            Pageable pageable = PageRequest.of(page, size);
            Page<DiaryResponse> response = diaryService.getDiaries(userId, pageable);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("일기 목록 조회 API 오류 - 사용자: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 일기 삭제
     * DELETE /api/diary/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiary(
            @PathVariable Long id,
            @RequestParam String userId) {

        try {
            log.info("일기 삭제 API 호출 - ID: {}, 사용자: {}", id, userId);

            diaryService.deleteDiary(id, userId);

            log.info("일기 삭제 성공 - ID: {}", id);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("일기 삭제 실패 - ID: {}, 오류: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("일기 삭제 API 오류 - ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 일기 검색 (키워드)
     * GET /api/diary/search
     */
    @GetMapping("/search")
    public ResponseEntity<List<DiaryResponse>> searchDiaries(
            @RequestParam String userId,
            @RequestParam String keyword) {

        try {
            log.info("일기 검색 API 호출 - 사용자: {}, 키워드: {}", userId, keyword);

            List<DiaryResponse> response = diaryService.searchDiaries(userId, keyword);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("일기 검색 API 오류 - 사용자: {}, 키워드: {}", userId, keyword, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 감정별 일기 조회
     * GET /api/diary/emotion/{emotion}
     */
    @GetMapping("/emotion/{emotion}")
    public ResponseEntity<List<DiaryResponse>> getDiariesByEmotion(
            @RequestParam String userId,
            @PathVariable String emotion) {

        try {
            log.info("감정별 일기 조회 API 호출 - 사용자: {}, 감정: {}", userId, emotion);

            List<DiaryResponse> response = diaryService.getDiariesByEmotion(userId, emotion);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("감정별 일기 조회 API 오류 - 사용자: {}, 감정: {}", userId, emotion, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 사용자 감정 통계 조회
     * GET /api/diary/statistics/emotion
     */
    @GetMapping("/statistics/emotion")
    public ResponseEntity<Map<String, Object>> getEmotionStatistics(@RequestParam String userId) {
        try {
            log.info("감정 통계 조회 API 호출 - 사용자: {}", userId);

            Map<String, Long> statistics = diaryService.getEmotionStatistics(userId);

            // 응답 형태 개선
            Map<String, Object> response = Map.of(
                    "userId", userId,
                    "statistics", statistics,
                    "totalDiaries", statistics.values().stream().mapToLong(Long::longValue).sum()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("감정 통계 조회 API 오류 - 사용자: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 파일 업로드 정보 조회 (프론트엔드용)
     * GET /api/diary/upload-info
     */
    @GetMapping("/upload-info")
    public ResponseEntity<Map<String, Object>> getUploadInfo() {
        Map<String, Object> uploadInfo = Map.of(
                "maxFileSize", fileUploadService.getMaxFileSize(),
                "maxFileSizeReadable", fileUploadService.getMaxFileSizeReadable(),
                "allowedExtensions", fileUploadService.getAllowedExtensions(),
                "uploadPath", fileUploadService.getUploadPath()
        );

        return ResponseEntity.ok(uploadInfo);
    }

    /**
     * API 상태 확인
     * GET /api/diary/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = Map.of(
                "status", "UP",
                "message", "Diary API is running!",
                "version", "1.0",
                "features", List.of(
                        "일기 CRUD",
                        "파일 업로드",
                        "감정 분석 연동",
                        "키워드 검색",
                        "감정별 필터링",
                        "통계 조회"
                )
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 테스트용 간단한 일기 생성 (JSON)
     * POST /api/diary/simple
     */
    @PostMapping("/simple")
    public ResponseEntity<DiaryResponse> createSimpleDiary(@Valid @RequestBody DiaryCreateRequest request) {
        try {
            log.info("간단한 일기 생성 API 호출 - 사용자: {}", request.getUserId());

            DiaryResponse response = diaryService.createDiary(request, null);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("간단한 일기 생성 API 오류", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}