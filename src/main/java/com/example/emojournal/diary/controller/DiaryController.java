package com.example.emojournal.diary.controller;

import com.example.emojournal.diary.dto.DiaryCreateRequest;
import com.example.emojournal.diary.dto.DiaryResponse;
import com.example.emojournal.diary.dto.DiaryUpdateRequest;
import com.example.emojournal.diary.service.DiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    /**
     * 일기 작성
     */
    @PostMapping
    public ResponseEntity<DiaryResponse> createDiary(
            @ModelAttribute DiaryCreateRequest request,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        return ResponseEntity.ok(diaryService.createDiary(request, imageFile));
    }

    /**
     * 일기 수정
     */
    @PutMapping("/{diaryId}")
    public ResponseEntity<DiaryResponse> updateDiary(
            @PathVariable Long diaryId,
            @RequestParam String userId,
            @ModelAttribute DiaryUpdateRequest request,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        return ResponseEntity.ok(diaryService.updateDiary(diaryId, userId, request, imageFile));
    }

    /**
     * 일기 상세 조회
     */
    @GetMapping("/{diaryId}")
    public ResponseEntity<DiaryResponse> getDiary(
            @PathVariable Long diaryId,
            @RequestParam String userId) {
        return ResponseEntity.ok(diaryService.getDiary(diaryId, userId));
    }

    /**
     * 일기 목록 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<Page<DiaryResponse>> getDiaries(
            @RequestParam String userId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(diaryService.getDiaries(userId, pageable));
    }

    /**
     * 일기 목록 조회 (전체)
     */
    @GetMapping("/all")
    public ResponseEntity<List<DiaryResponse>> getAllDiaries(@RequestParam String userId) {
        return ResponseEntity.ok(diaryService.getAllDiaries(userId));
    }

    /**
     * 1년 전 오늘 작성한 일기 조회
     */
    @GetMapping("/one-year-ago")
    public ResponseEntity<DiaryResponse> getOneYearAgoDiary(@RequestParam String userId) {
        return ResponseEntity.ok(diaryService.getOneYearAgoDiary(userId));
    }

    /**
     * 일기 삭제
     */
    @DeleteMapping("/{diaryId}")
    public ResponseEntity<Void> deleteDiary(
            @PathVariable Long diaryId,
            @RequestParam String userId) {
        diaryService.deleteDiary(diaryId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 키워드 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<DiaryResponse>> searchDiaries(
            @RequestParam String userId,
            @RequestParam String keyword) {
        return ResponseEntity.ok(diaryService.searchDiaries(userId, keyword));
    }

    /**
     * 감정별 일기 조회
     */
    @GetMapping("/emotion")
    public ResponseEntity<List<DiaryResponse>> getDiariesByEmotion(
            @RequestParam String userId,
            @RequestParam String emotion) {
        return ResponseEntity.ok(diaryService.getDiariesByEmotion(userId, emotion));
    }

    /**
     * 감정 통계 조회
     */
    @GetMapping("/emotion/statistics")
    public ResponseEntity<Map<String, Long>> getEmotionStatistics(@RequestParam String userId) {
        return ResponseEntity.ok(diaryService.getEmotionStatistics(userId));
    }
}
