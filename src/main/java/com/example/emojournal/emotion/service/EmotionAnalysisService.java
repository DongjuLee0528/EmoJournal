package com.example.emojournal.emotion.service;

import com.example.emojournal.emotion.dto.EmotionAnalysisRequest;
import com.example.emojournal.emotion.dto.EmotionAnalysisResponse;
import com.example.emojournal.emotion.gemini.GeminiApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmotionAnalysisService {

    private final GeminiApiClient geminiApiClient;

    /**
     * 일기 텍스트의 감정을 분석하여 8가지 감정 중 1개와 키워드 최대 2개, 감정 해석을 반환
     */
    public EmotionAnalysisResponse analyzeEmotion(EmotionAnalysisRequest request) {
        try {
            log.info("감정 분석 요청 처리 시작: {}",
                    request.getDiaryText().substring(0, Math.min(50, request.getDiaryText().length())));

            // 입력 텍스트 전처리
            String cleanedText = preprocessText(request.getDiaryText());

            // 1. Gemini API로 감정, 키워드 분석
            GeminiApiClient.EmotionAnalysisResult analysisResult =
                    geminiApiClient.analyzeEmotionWithKeywords(cleanedText);

            // 2. 감정 해석 생성 (전체 키워드 리스트 사용)
            String interpretation = geminiApiClient.generateEmotionInterpretation(
                    analysisResult.getEmotion(),
                    analysisResult.getKeywords(), // 전체 키워드 리스트 (감정키워드 + 일기키워드)
                    cleanedText
            );

            log.info("감정 분석 완료 - 감정: {}, 감정키워드: {}, 일기키워드: {}, 이미지: {}",
                    analysisResult.getEmotion(), analysisResult.getEmotionKeyword(),
                    analysisResult.getDiaryKeywords(), analysisResult.getImageFileName());

            return EmotionAnalysisResponse.success(
                    request.getDiaryText(),
                    analysisResult.getEmotion(),
                    analysisResult.getEmotionKeyword(),
                    analysisResult.getDiaryKeywords(),
                    analysisResult.getImageFileName(),
                    interpretation
            );

        } catch (Exception e) {
            log.error("감정 분석 중 오류 발생", e);
            return EmotionAnalysisResponse.failure(
                    request.getDiaryText(),
                    "감정 분석 중 오류가 발생했습니다: " + e.getMessage()
            );
        }
    }

    /**
     * 텍스트 전처리 (불필요한 공백, 특수문자 정리)
     */
    private String preprocessText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "내용 없음";
        }

        return text.trim()
                .replaceAll("\\s+", " ")  // 여러 공백을 하나로
                .replaceAll("[\\r\\n]+", " ")  // 줄바꿈을 공백으로
                .replaceAll("[^가-힣a-zA-Z0-9\\s.,!?]", ""); // 특수문자 제거
    }

    /**
     * 9가지 감정 목록 반환 (프론트엔드에서 사용할 수 있도록)
     */
    public List<String> getAvailableEmotions() {
        return List.of("기쁨", "슬픔", "분노", "두려움", "혐오감", "놀람", "신뢰감", "사랑", "혼합감정");
    }

    /**
     * 감정 유효성 검증
     */
    public boolean isValidEmotion(String emotion) {
        return getAvailableEmotions().contains(emotion);
    }

    /**
     * 감정별 기본 설명 반환
     */
    public String getEmotionDescription(String emotion) {
        return switch (emotion) {
            case "기쁨" -> "즐겁고 행복한 감정";
            case "슬픔" -> "우울하고 아쉬운 감정";
            case "분노" -> "화나고 짜증나는 감정";
            case "두려움" -> "불안하고 걱정되는 감정";
            case "혐오감" -> "불쾌하고 싫은 감정";
            case "놀람" -> "예상치 못한 놀라운 감정";
            case "신뢰감" -> "믿음직하고 안정된 감정";
            case "사랑" -> "따뜻하고 애정 어린 감정";
            case "혼합감정" -> "복합적이고 다양한 감정";
            default -> "알 수 없는 감정";
        };
    }
}