package com.example.emojournal.diary.controller;

import com.example.emojournal.diary.dto.DiaryCreateRequest;
import com.example.emojournal.diary.dto.DiaryResponse;
import com.example.emojournal.diary.dto.DiaryUpdateRequest;
import com.example.emojournal.diary.service.DiaryService;
import com.example.emojournal.diary.service.FileUploadService;
import jakarta.servlet.http.HttpServletRequest;
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

    // JWT 필터에서 설정한 memberId → userId로 가공
    private String getUserIdFromRequest(HttpServletRequest request) {
        Object attr = request.getAttribute("memberId");
        if (attr == null) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }
        return "member_" + attr;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DiaryResponse> createDiary(
            @Valid @ModelAttribute DiaryCreateRequest request,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            HttpServletRequest httpRequest) {

        String userId = getUserIdFromRequest(httpRequest);
        request.setUserId(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(diaryService.createDiary(request, imageFile));
    }

    @PostMapping("/simple")
    public ResponseEntity<DiaryResponse> createSimpleDiary(
            @Valid @RequestBody DiaryCreateRequest request,
            HttpServletRequest httpRequest) {

        String userId = getUserIdFromRequest(httpRequest);
        request.setUserId(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(diaryService.createDiary(request, null));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DiaryResponse> updateDiary(
            @PathVariable Long id,
            @Valid @ModelAttribute DiaryUpdateRequest request,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            HttpServletRequest httpRequest) {

        String userId = getUserIdFromRequest(httpRequest);
        return ResponseEntity.ok(diaryService.updateDiary(id, userId, request, imageFile));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiaryResponse> getDiary(@PathVariable Long id, HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        return ResponseEntity.ok(diaryService.getDiary(id, userId));
    }

    @GetMapping
    public ResponseEntity<Page<DiaryResponse>> getDiaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {

        String userId = getUserIdFromRequest(httpRequest);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(diaryService.getDiaries(userId, pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<List<DiaryResponse>> getAllDiaries(HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        return ResponseEntity.ok(diaryService.getAllDiaries(userId));
    }

    @GetMapping("/one-year-ago")
    public ResponseEntity<DiaryResponse> getOneYearAgoDiary(HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        return ResponseEntity.ok(diaryService.getOneYearAgoDiary(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiary(@PathVariable Long id, HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        diaryService.deleteDiary(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<DiaryResponse>> searchDiaries(
            @RequestParam String keyword,
            HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        return ResponseEntity.ok(diaryService.searchDiaries(userId, keyword));
    }

    @GetMapping("/emotion/{emotion}")
    public ResponseEntity<List<DiaryResponse>> getDiariesByEmotion(
            @PathVariable String emotion,
            HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        return ResponseEntity.ok(diaryService.getDiariesByEmotion(userId, emotion));
    }

    @GetMapping("/statistics/emotion")
    public ResponseEntity<Map<String, Object>> getEmotionStatistics(HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
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
