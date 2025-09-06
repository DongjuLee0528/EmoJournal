package com.example.emojournal.emotion.controller;

import com.example.emojournal.emotion.dto.EmotionAnalysisRequest;
import com.example.emojournal.emotion.dto.EmotionAnalysisResponse;
import com.example.emojournal.emotion.service.EmotionAnalysisService;
import com.example.emojournal.emotion.service.EmotionImageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/emotion")
@RequiredArgsConstructor
public class EmotionAnalysisController {

    private final EmotionAnalysisService emotionAnalysisService;
    private final EmotionImageService emotionImageService;

    private String getUserIdFromRequest(HttpServletRequest request) {
        Object memberId = request.getAttribute("memberId");
        if (memberId != null) {
            return "member_" + memberId;
        }
        throw new RuntimeException("인증되지 않은 사용자입니다.");
    }

    @PostMapping("/analyze")
    public ResponseEntity<EmotionAnalysisResponse> analyzeEmotion(
            @Valid @RequestBody EmotionAnalysisRequest request,
            HttpServletRequest httpRequest) {

        try {
            String userId = getUserIdFromRequest(httpRequest);

            log.info("감정 분석 API 호출 - 사용자: {}, 텍스트 길이: {}",
                    userId, request.getDiaryText().length());

            EmotionAnalysisResponse response = emotionAnalysisService.analyzeEmotion(request);

            if (response.isSuccess()) {
                // 수정된 부분: 존재하는 메서드들로 변경
                log.info("감정 분석 성공 - 메인 태그: {}, 전체 태그: {}, 이모지: {}",
                        response.getMainTag(), response.getEmotionTags(), response.getMainEmoji());
                return ResponseEntity.ok(response);
            } else {
                log.warn("감정 분석 실패: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }

        } catch (RuntimeException e) {
            if (e.getMessage().contains("인증되지 않은")) {
                log.error("감정 분석 API 인증 오류: {}", e.getMessage());
                return ResponseEntity.status(401).build();
            }
            log.error("감정 분석 API 런타임 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            log.error("감정 분석 API 처리 중 오류", e);
            EmotionAnalysisResponse errorResponse = EmotionAnalysisResponse.failure(
                    request.getDiaryText(),
                    "서버 오류가 발생했습니다: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getEmotionCategories() {
        try {
            List<String> emotions = emotionAnalysisService.getAvailableEmotions();

            Map<String, Map<String, String>> emotionDetails = Map.of(
                    "기쁨", Map.of("imageUrl", "/images/emotions/joy.png", "description", "즐겁고 행복한 감정"),
                    "슬픔", Map.of("imageUrl", "/images/emotions/sadness.png", "description", "우울하고 아쉬운 감정"),
                    "분노", Map.of("imageUrl", "/images/emotions/anger.png", "description", "화나고 짜증나는 감정"),
                    "두려움", Map.of("imageUrl", "/images/emotions/fear.png", "description", "불안하고 걱정되는 감정"),
                    "혐오감", Map.of("imageUrl", "/images/emotions/disgust.png", "description", "불쾌하고 싫은 감정"),
                    "놀람", Map.of("imageUrl", "/images/emotions/surprise.png", "description", "예상치 못한 놀라운 감정"),
                    "신뢰감", Map.of("imageUrl", "/images/emotions/trust.png", "description", "믿음직하고 안정된 감정"),
                    "사랑", Map.of("imageUrl", "/images/emotions/love.png", "description", "따뜻하고 애정 어린 감정"),
                    "혼합감정", Map.of("imageUrl", "/images/emotions/mixed.png", "description", "복합적이고 다양한 감정")
            );

            Map<String, Object> response = Map.of(
                    "emotions", emotions,
                    "details", emotionDetails,
                    "totalCount", emotions.size()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("감정 카테고리 조회 중 오류", e);
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "감정 카테고리를 가져오는 중 오류가 발생했습니다.")
            );
        }
    }

    @GetMapping("/categories/{emotion}")
    public ResponseEntity<Map<String, String>> getEmotionDetail(@PathVariable String emotion) {
        try {
            if (!emotionAnalysisService.isValidEmotion(emotion)) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "유효하지 않은 감정입니다: " + emotion)
                );
            }

            String description = emotionAnalysisService.getEmotionDescription(emotion);
            String imageUrl = emotionImageService.getEmotionImageUrl(emotion);

            Map<String, String> response = Map.of(
                    "emotion", emotion,
                    "imageUrl", imageUrl,
                    "description", description
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("감정 상세 정보 조회 중 오류", e);
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "감정 정보를 가져오는 중 오류가 발생했습니다.")
            );
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = Map.of(
                "status", "UP",
                "message", "Emotion Analysis API is running with JWT Authentication!",
                "supportedEmotions", emotionAnalysisService.getAvailableEmotions(),
                "version", "2.0 (9 emotion categories with JWT auth)",
                "imageBasePath", "/images/emotions/"
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testEmotion(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        String testText = request.getOrDefault("text", "");

        try {
            String userId = getUserIdFromRequest(httpRequest);
            log.info("감정 분석 테스트 - 사용자: {}, 입력: {}", userId, testText);

            String predictedEmotion = predictEmotionByKeywords(testText);
            String imageUrl = emotionImageService.getEmotionImageUrl(predictedEmotion);

            Map<String, Object> response = Map.of(
                    "inputText", testText,
                    "predictedEmotion", predictedEmotion,
                    "imageUrl", imageUrl,
                    "confidence", "테스트 모드",
                    "message", predictedEmotion + " 감정이 감지되었습니다!",
                    "userId", userId
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("인증되지 않은")) {
                log.error("감정 분석 테스트 API 인증 오류: {}", e.getMessage());
                return ResponseEntity.status(401).build();
            }
            log.error("감정 분석 테스트 API 런타임 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "테스트 중 오류가 발생했습니다.")
            );
        } catch (Exception e) {
            log.error("감정 분석 테스트 중 오류", e);
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "테스트 중 오류가 발생했습니다.")
            );
        }
    }

    private String predictEmotionByKeywords(String text) {
        String lowerText = text.toLowerCase();

        if (lowerText.contains("행복") || lowerText.contains("기쁨") || lowerText.contains("좋") ||
                lowerText.contains("즐거") || lowerText.contains("웃")) {
            return "기쁨";
        } else if (lowerText.contains("슬프") || lowerText.contains("우울") || lowerText.contains("힘들") ||
                lowerText.contains("아쉽") || lowerText.contains("눈물")) {
            return "슬픔";
        } else if (lowerText.contains("화") || lowerText.contains("분노") || lowerText.contains("짜증") ||
                lowerText.contains("열받") || lowerText.contains("억울")) {
            return "분노";
        } else if (lowerText.contains("무서") || lowerText.contains("불안") || lowerText.contains("걱정") ||
                lowerText.contains("두려") || lowerText.contains("떨리")) {
            return "두려움";
        } else if (lowerText.contains("싫") || lowerText.contains("혐오") || lowerText.contains("더러") ||
                lowerText.contains("역겹") || lowerText.contains("불쾌")) {
            return "혐오감";
        } else if (lowerText.contains("놀라") || lowerText.contains("깜짝") || lowerText.contains("예상치") ||
                lowerText.contains("갑자기") || lowerText.contains("충격")) {
            return "놀람";
        } else if (lowerText.contains("믿") || lowerText.contains("신뢰") || lowerText.contains("의지") ||
                lowerText.contains("안정") || lowerText.contains("든든")) {
            return "신뢰감";
        } else if (lowerText.contains("사랑") || lowerText.contains("애정") || lowerText.contains("따뜻") ||
                lowerText.contains("소중") || lowerText.contains("마음")) {
            return "사랑";
        } else {
            return "혼합감정";
        }
    }
}