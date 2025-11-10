package com.example.emojournal.emotion.service;

import com.example.emojournal.emotion.gemini.GeminiApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmotionAnalysisService {

    private final GeminiApiClient geminiApiClient;

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

    public List<String> getAvailableEmotions() {
        return List.of("기쁨", "슬픔", "분노", "두려움", "혐오감", "놀람", "신뢰감", "사랑", "혼합감정", "중립감정", "일반감정");
    }

    public boolean isValidEmotion(String emotion) {
        return getAvailableEmotions().contains(emotion);
    }

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