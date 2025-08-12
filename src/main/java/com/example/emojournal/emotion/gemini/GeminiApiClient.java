package com.example.emojournal.emotion.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GeminiApiClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    // 9가지 고정 감정 목록
    private static final List<String> EMOTION_CATEGORIES = List.of(
            "기쁨", "슬픔", "분노", "두려움", "혐오감", "놀람", "신뢰감", "사랑", "혼합감정"
    );

    public GeminiApiClient() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 일기 텍스트를 분석해서 8가지 감정 중 대표 감정 1개와 키워드 최대 2개를 반환
     */
    public EmotionAnalysisResult analyzeEmotionWithKeywords(String diaryText) {
        try {
            log.debug("감정 분석 시작: {}", diaryText);

            String prompt = String.format(
                    "다음 일기 내용을 분석해서 아래와 같이 응답해주세요:\n\n" +
                            "1. 대표 감정 (다음 9가지 중 1개만 선택): 기쁨, 슬픔, 분노, 두려움, 혐오감, 놀람, 신뢰감, 사랑, 혼합감정\n" +
                            "2. 감정 키워드 (1개, 선택한 감정과 관련된 구체적인 단어)\n" +
                            "3. 일기 키워드 (1~2개, 일기 내용의 핵심 단어나 상황)\n\n" +
                            "응답 형식:\n" +
                            "감정: [선택된 감정]\n" +
                            "감정키워드: [감정관련 키워드]\n" +
                            "일기키워드: [키워드1, 키워드2]\n\n" +
                            "일기 내용: %s",
                    diaryText
            );

            Map<String, Object> requestBody = createRequestBody(prompt);
            String response = callGeminiApi(requestBody);
            EmotionAnalysisResult result = parseEmotionResponse(response);

            log.debug("감정 분석 결과: 감정={}, 키워드={}", result.getEmotion(), result.getKeywords());
            return result;

        } catch (Exception e) {
            log.error("Gemini API 감정 분석 중 오류 발생", e);
            return new EmotionAnalysisResult("기쁨", "평온", List.of("일반"), "joy.png");
        }
    }

    /**
     * 커스텀 프롬프트로 감정 분석 (개인화 서비스용)
     */
    public EmotionAnalysisResult analyzeEmotionWithCustomPrompt(String customPrompt) {
        try {
            log.debug("커스텀 프롬프트 감정 분석 시작");

            Map<String, Object> requestBody = createRequestBody(customPrompt);
            String response = callGeminiApi(requestBody);
            EmotionAnalysisResult result = parseEmotionResponse(response);

            log.debug("커스텀 프롬프트 감정 분석 결과: 감정={}, 키워드={}", result.getEmotion(), result.getKeywords());
            return result;

        } catch (Exception e) {
            log.error("커스텀 프롬프트 감정 분석 중 오류 발생", e);
            return new EmotionAnalysisResult("기쁨", "평온", List.of("일반"), "joy.png");
        }
    }

    /**
     * 감정에 따른 감정 해석을 생성 (더 자세하고 따뜻한 톤으로)
     */
    public String generateEmotionInterpretation(String emotion, List<String> allKeywords, String diaryText) {
        try {
            log.debug("감정 해석 생성 시작: 감정={}, 키워드={}", emotion, allKeywords);

            String keywordText = String.join(", ", allKeywords);
            String prompt = String.format(
                    "다음 정보를 바탕으로 따뜻하고 공감하는 톤으로 감정 해석을 작성해주세요:\n\n" +
                            "감정: %s\n" +
                            "키워드: %s\n" +
                            "일기 내용: %s\n\n" +
                            "조건:\n" +
                            "- 100자 이내로 작성\n" +
                            "- 따뜻하고 위로가 되는 말투 사용\n" +
                            "- 구체적인 감정 상태와 상황을 언급\n" +
                            "- '오늘 하루는...' 또는 '지금 마음이...' 같은 자연스러운 시작\n" +
                            "- 공감과 격려의 메시지 포함",
                    emotion, keywordText, diaryText.substring(0, Math.min(150, diaryText.length()))
            );

            Map<String, Object> requestBody = createRequestBody(prompt);
            String response = callGeminiApi(requestBody);
            String interpretation = parseResponse(response);

            log.debug("감정 해석 결과: {}", interpretation);
            return interpretation;

        } catch (Exception e) {
            log.error("Gemini API 감정 해석 생성 중 오류 발생", e);
            return getDefaultInterpretation(emotion);
        }
    }

    /**
     * 커스텀 프롬프트로 Gemini API 호출하여 텍스트 응답 받기 (개인화 서비스용)
     */
    public String callGeminiApiWithPrompt(String customPrompt) {
        try {
            log.debug("커스텀 프롬프트 API 호출 시작");

            Map<String, Object> requestBody = createRequestBody(customPrompt);
            String response = callGeminiApi(requestBody);
            String result = parseResponse(response);

            log.debug("커스텀 프롬프트 API 호출 완료");
            return result;

        } catch (Exception e) {
            log.error("커스텀 프롬프트 API 호출 중 오류 발생", e);
            return "개인화된 해석을 생성할 수 없습니다.";
        }
    }

    /**
     * 감정에 어울리는 이미지 파일명을 반환
     */
    public String getEmotionImageFileName(String emotion) {
        Map<String, String> emotionImageMap = Map.of(
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

        return emotionImageMap.getOrDefault(emotion, "joy.png");
    }

    /**
     * 감정에 어울리는 이모지를 반환 (하위 호환성용)
     */
    public String getEmotionEmoji(String emotion) {
        Map<String, String> emotionEmojiMap = Map.of(
                "기쁨", "😊",
                "슬픔", "😢",
                "분노", "😠",
                "두려움", "😰",
                "혐오감", "🤢",
                "놀람", "😲",
                "신뢰감", "🤝",
                "사랑", "❤️",
                "혼합감정", "😐"
        );

        return emotionEmojiMap.getOrDefault(emotion, "😐");
    }

    /**
     * Gemini API 응답에서 감정과 키워드 파싱
     */
    private EmotionAnalysisResult parseEmotionResponse(String response) {
        try {
            String content = parseResponse(response);

            String emotion = "기쁨"; // 기본값
            String emotionKeyword = "평온"; // 기본값
            List<String> diaryKeywords = List.of("일반"); // 기본값

            // 감정 추출
            if (content.contains("감정:")) {
                String emotionLine = extractLine(content, "감정:");
                for (String validEmotion : EMOTION_CATEGORIES) {
                    if (emotionLine.contains(validEmotion)) {
                        emotion = validEmotion;
                        break;
                    }
                }
            }

            // 감정 키워드 추출
            if (content.contains("감정키워드:")) {
                String emotionKeywordLine = extractLine(content, "감정키워드:");
                if (!emotionKeywordLine.isEmpty()) {
                    emotionKeyword = emotionKeywordLine.trim();
                }
            }

            // 일기 키워드 추출
            if (content.contains("일기키워드:")) {
                String diaryKeywordLine = extractLine(content, "일기키워드:");
                diaryKeywords = parseDiaryKeywords(diaryKeywordLine);
            }

            String imageFileName = getEmotionImageFileName(emotion);

            return new EmotionAnalysisResult(emotion, emotionKeyword, diaryKeywords, imageFileName);

        } catch (Exception e) {
            log.error("감정 응답 파싱 중 오류", e);
            return new EmotionAnalysisResult("기쁨", "평온", List.of("일반"), "joy.png");
        }
    }

    /**
     * 텍스트에서 특정 라벨의 라인 추출
     */
    private String extractLine(String content, String label) {
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.contains(label)) {
                return line.replace(label, "").trim();
            }
        }
        return "";
    }

    /**
     * 일기 키워드 문자열을 리스트로 파싱 (1~2개)
     */
    private List<String> parseDiaryKeywords(String keywordLine) {
        if (keywordLine.isEmpty()) {
            return List.of("일반");
        }

        List<String> keywords = List.of(keywordLine.split("[,\\s]+"))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .limit(2) // 최대 2개
                .toList();

        return keywords.isEmpty() ? List.of("일반") : keywords;
    }

    /**
     * 기본 감정 해석 반환
     */
    private String getDefaultInterpretation(String emotion) {
        Map<String, String> defaultInterpretations = Map.of(
                "기쁨", "오늘 하루도 즐거운 시간을 보내셨네요!",
                "슬픔", "힘든 하루였지만 내일은 더 나아질 거예요.",
                "분노", "화가 나는 일이 있었군요. 잠시 쉬어가세요.",
                "두려움", "불안한 마음이 드시는군요. 괜찮을 거예요.",
                "혐오감", "불쾌한 경험을 하셨군요.",
                "놀람", "예상치 못한 일이 있었나 보네요!",
                "신뢰감", "믿음직한 하루를 보내셨네요.",
                "사랑", "따뜻한 마음이 느껴지는 하루였군요.",
                "혼합감정", "복잡한 마음이 드는 하루였네요."
        );

        return defaultInterpretations.getOrDefault(emotion, "오늘도 수고하셨어요.");
    }

    /**
     * Gemini API 호출 (공통 메서드)
     */
    private String callGeminiApi(Map<String, Object> requestBody) {
        return webClient.post()
                .uri(apiUrl + "?key=" + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /**
     * Gemini API 요청 본문 생성
     */
    private Map<String, Object> createRequestBody(String prompt) {
        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(Map.of("text", prompt)));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        // 응답 설정
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.3);
        generationConfig.put("maxOutputTokens", 200);
        requestBody.put("generationConfig", generationConfig);

        return requestBody;
    }

    /**
     * Gemini API 응답 파싱
     */
    private String parseResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode candidates = jsonNode.get("candidates");

            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        return parts.get(0).get("text").asText();
                    }
                }
            }

            return "분석 결과를 가져올 수 없습니다.";

        } catch (Exception e) {
            log.error("응답 파싱 중 오류", e);
            return "분석 결과를 가져올 수 없습니다.";
        }
    }

    // 하위 호환성을 위한 기존 메서드들 (deprecated)
    @Deprecated
    public String analyzeEmotionTags(String diaryText) {
        EmotionAnalysisResult result = analyzeEmotionWithKeywords(diaryText);
        return "#" + result.getEmotion();
    }

    @Deprecated
    public String generateMainEmoji(String mainTag) {
        String emotion = mainTag.replace("#", "");
        return getEmotionEmoji(emotion);
    }

    @Deprecated
    public String analyzeEmotion(String diaryText) {
        return analyzeEmotionTags(diaryText);
    }

    /**
     * 감정 분석 결과를 담는 내부 클래스
     */
    public static class EmotionAnalysisResult {
        private final String emotion;
        private final String emotionKeyword;
        private final List<String> diaryKeywords;
        private final String imageFileName;

        public EmotionAnalysisResult(String emotion, String emotionKeyword, List<String> diaryKeywords, String imageFileName) {
            this.emotion = emotion;
            this.emotionKeyword = emotionKeyword;
            this.diaryKeywords = diaryKeywords;
            this.imageFileName = imageFileName;
        }

        public String getEmotion() { return emotion; }
        public String getEmotionKeyword() { return emotionKeyword; }
        public List<String> getDiaryKeywords() { return diaryKeywords; }
        public String getImageFileName() { return imageFileName; }

        // 전체 키워드 리스트 반환 (하위 호환성)
        public List<String> getKeywords() {
            List<String> allKeywords = new java.util.ArrayList<>();
            allKeywords.add(emotionKeyword);
            allKeywords.addAll(diaryKeywords);
            return allKeywords;
        }

        // 기존 이모지 메서드 (하위 호환성)
        public String getEmoji() {
            Map<String, String> emotionEmojiMap = Map.of(
                    "기쁨", "😊", "슬픔", "😢", "분노", "😠", "두려움", "😰",
                    "혐오감", "🤢", "놀람", "😲", "신뢰감", "🤝", "사랑", "❤️", "혼합감정", "😐"
            );
            return emotionEmojiMap.getOrDefault(emotion, "😐");
        }
    }
}