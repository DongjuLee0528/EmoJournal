package com.example.emojournal.emotion.service;

import com.example.emojournal.emotion.gemini.GeminiApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 감정 분석 다사 서비스
 *
 * Gemini API 클라이언트를 통한 AI 감정 분석과 감정 카테고리 관리 기능을 제공합니다.
 * 일기 텍스트를 분석하여 감정 키워드, 이모지, 해석을 추출하고 관련 이미지 파일명을 매핑합니다.
 *
 * 주요 기능:
 * - AI 기반 일기 감정 분석
 * - 감정별 이미지 파일 매핑
 * - 지원 가능한 감정 카테고리 관리
 * - 감정 유효성 검증 및 설명 제공
 *
 * @author EmoJournal Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmotionAnalysisService {

    /** Google Gemini API를 통한 실제 감정 분석을 수행하는 클라이언트 */
    private final GeminiApiClient geminiApiClient;

    /**
     * 일기 텍스트에 대한 감정 분석을 수행합니다.
     * Gemini API를 통해 감정을 분석하고, 결과에 맞는 이미지 파일명을 매핑합니다.
     *
     * @param diaryText 분석할 일기 텍스트
     * @return 감정 분석 결과 (감정 키워드, 이모지, 해석, 이미지 파일명 포함)
     */
    public EmotionAnalysisResult analyzeEmotion(String diaryText) {
        try {
            GeminiApiClient.EmotionAnalysisResult result = geminiApiClient.analyzeEmotion(diaryText);
            String imageFileName = getEmotionImageFileName(result.getMainTag());

            return new EmotionAnalysisResult(
                    result.getMainTag(),
                    result.getKeywords(),
                    result.getEmoji(),
                    imageFileName,
                    result.getInterpretation()
            );

        } catch (Exception e) {
            log.warn("감정 분석 실패: {}", e.getMessage());
            return new EmotionAnalysisResult(null, List.of(), null, null, null);
        }
    }

    /**
     * 감정 카테고리에 대응하는 이미지 파일명을 반환합니다.
     * 각 감정별로 미리 정의된 이미지 파일명을 매핑하여 반환합니다.
     *
     * @param emotion 감정 카테고리 명
     * @return 대응하는 이미지 파일명 (emotion이 null이거나 없는 감정이면 null)
     */
    private String getEmotionImageFileName(String emotion) {
        if (emotion == null) return null;

        Map<String, String> emotionImageMap = new HashMap<>();
        emotionImageMap.put("기쁨", "joy.png");
        emotionImageMap.put("슬픔", "sadness.png");
        emotionImageMap.put("분노", "anger.png");
        emotionImageMap.put("두려움", "fear.png");
        emotionImageMap.put("혐오감", "disgust.png");
        emotionImageMap.put("놀람", "surprise.png");
        emotionImageMap.put("신뢰감", "trust.png");
        emotionImageMap.put("사랑", "love.png");
        emotionImageMap.put("혼합감정", "mixed.png");
        emotionImageMap.put("중립감정", "neutral.png");
        emotionImageMap.put("일반감정", "standard.png");

        return emotionImageMap.get(emotion);
    }

    /**
     * 지원 가능한 모든 감정 카테고리 목록을 반환합니다.
     * 시스템에서 인식 가능한 감정 브리지 목록입니다.
     *
     * @return 지원 가능한 감정 카테고리 문자열 리스트
     */
    public List<String> getAvailableEmotions() {
        return List.of("기쁨", "슬픔", "분노", "두려움", "혐오감", "놀람", "신뢰감", "사랑", "혼합감정", "중립감정", "일반감정");
    }

    /**
     * 주어진 감정이 지원 가능한 감정 카테고리인지 검증합니다.
     *
     * @param emotion 검증할 감정 문자열
     * @return 지원 가능한 감정이면 true, 아니면 false
     */
    public boolean isValidEmotion(String emotion) {
        return getAvailableEmotions().contains(emotion);
    }

    /**
     * 감정 카테고리에 대한 한국어 설명을 반환합니다.
     * 각 감정의 특징과 의미를 사용자가 이해하기 쉬도록 설명합니다.
     *
     * @param emotion 설명을 받을 감정 카테고리
     * @return 감정에 대한 한국어 설명 문자열
     */
    public String getEmotionDescription(String emotion) {
        switch (emotion) {
            case "기쁨": return "즐겁고 행복한 감정";
            case "슬픔": return "우울하고 아쉬운 감정";
            case "분노": return "화나고 짜증나는 감정";
            case "두려움": return "불안하고 걱정되는 감정";
            case "혐오감": return "불쾌하고 싫은 감정";
            case "놀람": return "예상치 못한 놀라운 감정";
            case "신뢰감": return "믿음직하고 안정된 감정";
            case "사랑": return "따뜻하고 애정 어린 감정";
            case "혼합감정": return "복합적이고 다양한 감정";
            case "중립감정": return "중성적인 감정";
            case "일반감정": return "일반적인 감정";
            default: return "알 수 없는 감정입니다.";
        }
    }

    /**
     * 감정 분석 결과를 담는 불변 데이터 클래스
     *
     * AI 분석 결과인 주요 감정 키워드, 서브 키워드, 이모지, 해석 및
     * 대응하는 이미지 파일명을 안전하게 묶어 반환하는 객체입니다.
     */
    public static class EmotionAnalysisResult {
        private final String mainTag;
        private final List<String> subTags;
        private final String emoji;
        private final String imageFile;
        private final String interpretation;

        public EmotionAnalysisResult(String mainTag, List<String> subTags, String emoji, String imageFile, String interpretation) {
            this.mainTag = mainTag;
            this.subTags = subTags != null ? subTags : List.of();
            this.emoji = emoji;
            this.imageFile = imageFile;
            this.interpretation = interpretation;
        }

        public String getMainTag() { return mainTag; }
        public List<String> getSubTags() { return subTags; }
        public String getEmoji() { return emoji; }
        public String getImageFile() { return imageFile; }
        public String getInterpretation() { return interpretation; }
    }
}