package com.example.emojournal.emotion.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class EmotionImageService {

    private static final String IMAGE_BASE_PATH = "/images/emotions/";

    // 감정별 이미지 파일 매핑
    private static final Map<String, String> EMOTION_IMAGE_MAP = Map.of(
            "기쁨", "joy.png",
            "슬픔", "sadness.png",
            "분노", "anger.png",
            "두려움", "fear.png",
            "혐오감", "disgust.png",
            "놀람", "surprise.png",
            "신뢰감", "trust.png",
            "사랑", "love.png",
            "혼합감정", "mixed.png"
    );

    /**
     * 감정에 해당하는 이미지 파일명 반환
     */
    public String getEmotionImageFileName(String emotion) {
        return EMOTION_IMAGE_MAP.getOrDefault(emotion, "joy.png");
    }

    /**
     * 감정에 해당하는 이미지 URL 경로 반환 (정적 파일 경로)
     */
    public String getEmotionImageUrl(String emotion) {
        String fileName = getEmotionImageFileName(emotion);
        return IMAGE_BASE_PATH + fileName;
    }

    /**
     * 모든 감정의 이미지 URL 매핑 반환
     */
    public Map<String, String> getAllEmotionImageUrls() {
        return EMOTION_IMAGE_MAP.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> IMAGE_BASE_PATH + entry.getValue()
                ));
    }

    /**
     * 지원하는 감정 목록 반환
     */
    public java.util.Set<String> getSupportedEmotions() {
        return EMOTION_IMAGE_MAP.keySet();
    }

    /**
     * 감정 유효성 검증
     */
    public boolean isValidEmotion(String emotion) {
        return EMOTION_IMAGE_MAP.containsKey(emotion);
    }
}