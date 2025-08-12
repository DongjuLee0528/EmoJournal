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

    public GeminiApiClient() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 일기 텍스트를 분석해서 감정 해시태그를 반환
     */
    public String analyzeEmotionTags(String diaryText) {
        try {
            log.debug("감정 해시태그 분석 시작: {}", diaryText);

            String prompt = String.format(
                    "다음 일기 내용을 분석해서 감정을 해시태그 형태로 3개 이하로 추출해주세요. " +
                            "결과는 '#행복 #기쁨' 형태로만 응답해주세요.\n\n일기 내용: %s",
                    diaryText
            );

            Map<String, Object> requestBody = createRequestBody(prompt);
            String response = callGeminiApi(requestBody);
            String result = parseResponse(response);

            log.debug("감정 해시태그 결과: {}", result);
            return result;

        } catch (Exception e) {
            log.error("Gemini API 해시태그 분석 중 오류 발생", e);
            return "#중성";
        }
    }

    /**
     * 대표 태그에 어울리는 이모지를 생성
     */
    public String generateMainEmoji(String mainTag) {
        try {
            log.debug("대표 태그 이모지 생성 시작: {}", mainTag);

            String prompt = String.format(
                    "다음 감정 해시태그에 어울리는 이모지를 1개만 생성해주세요. " +
                            "이모지만 응답해주세요 (예: 😊).\n\n대표 태그: %s",
                    mainTag
            );

            Map<String, Object> requestBody = createRequestBody(prompt);
            String response = callGeminiApi(requestBody);
            String result = parseResponse(response);

            // 이모지만 추출 (첫 번째 이모지만)
            String cleanedEmoji = result.replaceAll("[^\\p{So}\\p{Cn}]", "").trim();
            if (cleanedEmoji.isEmpty()) {
                cleanedEmoji = getDefaultEmoji(mainTag);
            } else {
                // 첫 번째 이모지만 사용
                cleanedEmoji = cleanedEmoji.substring(0, Math.min(2, cleanedEmoji.length()));
            }

            log.debug("대표 태그 이모지 생성 결과: {}", cleanedEmoji);
            return cleanedEmoji;

        } catch (Exception e) {
            log.error("Gemini API 이모지 생성 중 오류 발생", e);
            return getDefaultEmoji(mainTag);
        }
    }

    /**
     * 기본 이모지 반환 (API 실패 시)
     */
    private String getDefaultEmoji(String tag) {
        String lowerTag = tag.toLowerCase().replace("#", "");

        if (lowerTag.contains("행복") || lowerTag.contains("기쁨") || lowerTag.contains("즐거")) {
            return "😊";
        } else if (lowerTag.contains("슬프") || lowerTag.contains("우울")) {
            return "😢";
        } else if (lowerTag.contains("화") || lowerTag.contains("분노")) {
            return "😠";
        } else if (lowerTag.contains("불안") || lowerTag.contains("걱정")) {
            return "😰";
        } else if (lowerTag.contains("사랑") || lowerTag.contains("애정")) {
            return "❤️";
        } else {
            return "😐";
        }
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
     * 기존 analyzeEmotion 메서드 (하위 호환성을 위해 유지)
     */
    public String analyzeEmotion(String diaryText) {
        return analyzeEmotionTags(diaryText);
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
        generationConfig.put("maxOutputTokens", 100);
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
                        String text = parts.get(0).get("text").asText();
                        return cleanEmotionTags(text);
                    }
                }
            }

            return "#중성";

        } catch (Exception e) {
            log.error("응답 파싱 중 오류", e);
            return "#중성";
        }
    }

    /**
     * 감정 태그 정리 (불필요한 텍스트 제거)
     */
    private String cleanEmotionTags(String text) {
        // 해시태그만 추출하는 로직
        String cleaned = text.replaceAll("[^#가-힣\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();

        if (cleaned.isEmpty() || !cleaned.contains("#")) {
            return "#중성";
        }

        return cleaned;
    }
}