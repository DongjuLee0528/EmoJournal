package com.example.emojournal.emotion.controller;

// 수정된 import 문들
import com.example.emojournal.emotion.dto.EmotionAnalysisRequest;
import com.example.emojournal.emotion.dto.EmotionAnalysisResponse;
import com.example.emojournal.emotion.service.EmotionAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/emotion")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 프론트엔드 연동을 위한 CORS 설정
public class EmotionAnalysisController {

    private final EmotionAnalysisService emotionAnalysisService;

    /**
     * 일기 텍스트 감정 분석 API
     * POST /api/emotion/analyze
     */
    @PostMapping("/analyze")
    public ResponseEntity<EmotionAnalysisResponse> analyzeEmotion(
            @Valid @RequestBody EmotionAnalysisRequest request) {

        log.info("감정 분석 API 호출 - 사용자: {}, 텍스트 길이: {}",
                request.getUserId(), request.getDiaryText().length());

        try {
            EmotionAnalysisResponse response = emotionAnalysisService.analyzeEmotion(request);

            if (response.isSuccess()) {
                log.info("감정 분석 성공 - 대표태그: {}, 서브태그: {}, 이모지: {}",
                        response.getMainTag(), response.getSubTags(), response.getMainEmoji());
                return ResponseEntity.ok(response);
            } else {
                log.warn("감정 분석 실패: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("감정 분석 API 처리 중 오류", e);
            EmotionAnalysisResponse errorResponse = EmotionAnalysisResponse.failure(
                    request.getDiaryText(),
                    "서버 오류가 발생했습니다: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * API 상태 확인
     * GET /api/emotion/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Emotion Analysis API is running!");
    }

    /**
     * 감정 분석 테스트용 API (간단한 테스트)
     * POST /api/emotion/test
     */
    @PostMapping("/test")
    public ResponseEntity<String> testEmotion(@RequestBody String testText) {
        log.info("감정 분석 테스트 - 입력: {}", testText);

        // 간단한 키워드 기반 테스트
        String result;
        if (testText.contains("행복") || testText.contains("기쁨") || testText.contains("좋")) {
            result = "긍정적인 감정이 감지되었습니다! 😊";
        } else if (testText.contains("슬프") || testText.contains("우울") || testText.contains("힘들")) {
            result = "부정적인 감정이 감지되었습니다. 😢";
        } else {
            result = "중성적인 감정입니다. 😐";
        }

        return ResponseEntity.ok(result);
    }
}