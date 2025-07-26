package com.example.emojournal.emotion.service;

import com.example.emojournal.emotion.dto.EmotionAnalysisRequest;
import com.example.emojournal.emotion.dto.EmotionAnalysisResponse;
import com.example.emojournal.emotion.gemini.GeminiApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmotionAnalysisService {

    private final GeminiApiClient geminiApiClient;

    /**
     * 일기 텍스트의 감정을 분석하여 해시태그, 대표/서브 태그, 이모지를 모두 반환
     */
    public EmotionAnalysisResponse analyzeEmotion(EmotionAnalysisRequest request) {
        try {
            log.info("감정 분석 요청 처리 시작: {}", request.getDiaryText().substring(0, Math.min(50, request.getDiaryText().length())));

            // 입력 텍스트 전처리
            String cleanedText = preprocessText(request.getDiaryText());

            // 1. 감정 해시태그 생성 (1~3개)
            String rawEmotionTags = geminiApiClient.analyzeEmotionTags(cleanedText);
            String emotionTags = validateAndCleanEmotionTags(rawEmotionTags);

            // 2. 해시태그를 리스트로 변환
            List<String> tagList = parseEmotionTags(emotionTags);

            // 3. 대표 태그와 서브 태그 분리
            String mainTag = getMainTag(tagList);
            List<String> subTags = getSubTags(tagList);

            // 4. 대표 태그에 어울리는 이모지 생성
            String mainEmoji = geminiApiClient.generateMainEmoji(mainTag);

            log.info("감정 분석 완료 - 대표태그: {}, 서브태그: {}, 이모지: {}",
                    mainTag, subTags, mainEmoji);

            return EmotionAnalysisResponse.success(
                    request.getDiaryText(),
                    emotionTags,
                    tagList,
                    mainTag,
                    subTags,
                    mainEmoji
            );

        } catch (Exception e) {
            log.error("감정 분석 중 오류 발생", e);
            return EmotionAnalysisResponse.failure(request.getDiaryText(), "감정 분석 중 오류가 발생했습니다: " + e.getMessage());
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
                .replaceAll("[\\r\\n]+", " ");  // 줄바꿈을 공백으로
    }

    /**
     * 감정 해시태그 문자열을 리스트로 파싱
     * 예: "#행복 #기쁨 #만족" → ["행복", "기쁨", "만족"]
     */
    private List<String> parseEmotionTags(String emotionTags) {
        if (emotionTags == null || emotionTags.trim().isEmpty()) {
            return List.of("중성");
        }

        return Arrays.stream(emotionTags.split("\\s+"))
                .map(tag -> tag.replace("#", "").trim())
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 대표 태그 추출 (첫 번째 태그)
     */
    private String getMainTag(List<String> tagList) {
        if (tagList == null || tagList.isEmpty()) {
            return "#중성";
        }
        return "#" + tagList.get(0);
    }

    /**
     * 서브 태그들 추출 (두 번째부터)
     */
    private List<String> getSubTags(List<String> tagList) {
        if (tagList == null || tagList.size() <= 1) {
            return List.of();
        }

        return tagList.subList(1, tagList.size()).stream()
                .map(tag -> "#" + tag)
                .collect(Collectors.toList());
    }

    /**
     * 감정 분석 결과 검증 및 후처리 (1~3개 제한)
     */
    private String validateAndCleanEmotionTags(String emotionTags) {
        if (emotionTags == null || emotionTags.trim().isEmpty() || !emotionTags.contains("#")) {
            return "#중성";
        }

        // 중복 제거 및 정리 (1~3개로 제한)
        List<String> uniqueTags = Arrays.stream(emotionTags.split("\\s+"))
                .map(tag -> tag.trim())
                .filter(tag -> tag.startsWith("#") && tag.length() > 1)
                .distinct()
                .limit(3)  // 최대 3개까지만
                .collect(Collectors.toList());

        // 최소 1개는 보장
        if (uniqueTags.isEmpty()) {
            return "#중성";
        }

        return String.join(" ", uniqueTags);
    }
}