package com.example.emojournal.emotion.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Component
public class GeminiApiClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.base-url}")
    private String baseUrl;

    @Value("${gemini.model}")
    private String model;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final Pattern codeBlockPattern = Pattern.compile("^```(json)?\\s*|\\s*```$", Pattern.MULTILINE);

    public GeminiApiClient() {
        this.webClient = WebClient.builder()
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public EmotionAnalysisResult analyzeEmotion(String diaryText) {
        try {
            String preamble = "당신의 출력은 반드시 단 하나의 JSON 객체여야 합니다.\n" +
                    "설명/마크다운/코드펜스/문자 장식 금지. 공백 외의 어떤 문자도 JSON 바깥에 출력하지 마세요.\n" +
                    "스키마:\n" +
                    "{\n" +
                    "  \"mainTag\": string|null,          // 한국어 단일 감정 키워드(예: \"기쁨\"), 없으면 null\n" +
                    "  \"emoji\": string|null,            // 단일 이모지(예: \"😊\"), 없으면 null\n" +
                    "  \"keywords\": string[],            // 연관 키워드 배열, 없으면 []\n" +
                    "  \"interpretation\": string|null    // 1~2문장 요약, 없으면 null\n" +
                    "}\n" +
                    "입력 일기만 고려하세요. 안전필터로 직접 답변이 제한되면, 위 스키마를 유지하되 가능한 필드에 null/[]만 채워 JSON으로만 응답하세요.\n" +
                    "You must respond with ONLY a single JSON object, no code fences, no prose.\n\n";

            String prompt = preamble + diaryText;

            Map<String, Object> requestBody = createRequestBody(prompt);
            String response = callGeminiApi(requestBody);
            return parseEmotionResponse(response);

        } catch (Exception e) {
            log.error("감정 분석 실패: {}", e.getMessage());
            throw new GeminiAnalysisException("감정 분석 실패", e);
        }
    }

    private String callGeminiApi(Map<String, Object> requestBody) {
        return callGeminiApiWithModel(requestBody, model);
    }

    private String callGeminiApiWithModel(Map<String, Object> requestBody, String modelToUse) {
        String url = String.format("%s/v1beta/models/%s:generateContent", baseUrl, modelToUse);

        return webClient.post()
                .uri(url)
                .header("x-goog-api-key", apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                        .filter(throwable -> {
                            if (throwable instanceof WebClientResponseException) {
                                int status = ((WebClientResponseException) throwable).getStatusCode().value();
                                return status == 429 || (status >= 500 && status < 600);
                            }
                            return false;
                        }))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    int statusCode = ex.getStatusCode().value();
                    log.info("상태코드: {}, 모델: {}", statusCode, modelToUse);

                    if (statusCode == 404 && modelToUse.equals(model)) {
                        log.warn("404 대체모델 사용: gemini-1.5-flash-001");
                        return callGeminiApiWithModelInternal(requestBody, "gemini-1.5-flash-001");
                    }

                    return Mono.error(new GeminiAnalysisException("API 호출 실패: " + statusCode));
                })
                .subscribeOn(Schedulers.boundedElastic())
                .block();
    }

    private Mono<String> callGeminiApiWithModelInternal(Map<String, Object> requestBody, String modelToUse) {
        String url = String.format("%s/v1beta/models/%s:generateContent", baseUrl, modelToUse);

        return webClient.post()
                .uri(url)
                .header("x-goog-api-key", apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                        .filter(throwable -> {
                            if (throwable instanceof WebClientResponseException) {
                                int status = ((WebClientResponseException) throwable).getStatusCode().value();
                                return status == 429 || (status >= 500 && status < 600);
                            }
                            return false;
                        }));
    }

    private EmotionAnalysisResult parseEmotionResponse(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode candidates = rootNode.get("candidates");

            if (candidates == null || !candidates.isArray() || candidates.size() == 0) {
                throw new GeminiAnalysisException("후보 응답이 없음");
            }

            JsonNode selectedCandidate = selectValidCandidate(candidates);
            String rawText = extractRawText(selectedCandidate);
            String normalizedText = normalizeText(rawText);

            log.info("상태코드: 200, 모델: {}, 후보 개수: {}, Raw ({}자): {}",
                    model, candidates.size(), normalizedText.length(),
                    normalizedText.length() > 500 ? normalizedText.substring(0, 500) + "..." : normalizedText);

            return parseJsonResponse(normalizedText);

        } catch (Exception e) {
            log.error("응답 파싱 실패: {}", e.getMessage());
            throw new GeminiAnalysisException("응답 파싱 실패", e);
        }
    }

    private JsonNode selectValidCandidate(JsonNode candidates) {
        for (JsonNode candidate : candidates) {
            if (isValidCandidate(candidate)) {
                return candidate;
            }
        }
        throw new GeminiAnalysisException("유효한 후보가 없음");
    }

    private boolean isValidCandidate(JsonNode candidate) {
        JsonNode content = candidate.get("content");
        if (content == null) {
            JsonNode text = candidate.get("text");
            return text != null;
        }

        JsonNode parts = content.get("parts");
        JsonNode text = content.get("text");
        if ((parts == null || !parts.isArray() || parts.size() == 0) && text == null) {
            return false;
        }

        JsonNode finishReason = candidate.get("finishReason");
        if (finishReason != null && !finishReason.asText().isEmpty() && !"STOP".equals(finishReason.asText())) {
            return false;
        }

        return true;
    }

    private String extractRawText(JsonNode candidate) {
        StringBuilder rawText = new StringBuilder();

        JsonNode content = candidate.get("content");
        if (content != null) {
            JsonNode parts = content.get("parts");
            if (parts != null && parts.isArray()) {
                for (JsonNode part : parts) {
                    JsonNode textNode = part.get("text");
                    if (textNode != null) {
                        rawText.append(textNode.asText());
                    }
                }
            }

            if (rawText.length() == 0) {
                JsonNode textNode = content.get("text");
                if (textNode != null) {
                    rawText.append(textNode.asText());
                }
            }
        } else {
            JsonNode textNode = candidate.get("text");
            if (textNode != null) {
                rawText.append(textNode.asText());
            }
        }

        return rawText.toString();
    }

    private String normalizeText(String rawText) {
        if (rawText == null) return "";

        String normalized = rawText.trim();
        normalized = codeBlockPattern.matcher(normalized).replaceAll("");
        normalized = normalized.replaceAll("[\u0000-\u001F\u007F\uFEFF]", "");

        return normalized.trim();
    }

    private String extractBalancedJsonSubstring(String text) {
        // 빠른 JSON 스니핑: { 와 } 가 모두 없으면 즉시 예외
        if (!text.contains("{") || !text.contains("}")) {
            throw new GeminiAnalysisException("JSON 형식이 아님 - 브레이스 없음");
        }

        int firstBrace = text.indexOf('{');
        if (firstBrace == -1) {
            throw new GeminiAnalysisException("JSON 시작 브레이스를 찾을 수 없음");
        }

        int braceCount = 0;
        int start = firstBrace;

        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{') {
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    return text.substring(start, i + 1);
                }
            }
        }

        throw new GeminiAnalysisException("균형 잡힌 JSON 블록을 찾을 수 없음");
    }

    private EmotionAnalysisResult parseJsonResponse(String normalizedText) {
        try {
            String jsonText = extractBalancedJsonSubstring(normalizedText);
            JsonNode jsonNode = objectMapper.readTree(jsonText);

            String mainTag = getStringValue(jsonNode, "mainTag");
            String emoji = getStringValue(jsonNode, "emoji");
            List<String> keywords = getStringArrayValue(jsonNode, "keywords");
            String interpretation = getStringValue(jsonNode, "interpretation");

            return new EmotionAnalysisResult(mainTag, emoji, keywords, interpretation);

        } catch (Exception e) {
            throw new GeminiAnalysisException("JSON 파싱 실패", e);
        }
    }

    private String getStringValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull()) ? field.asText() : null;
    }

    private List<String> getStringArrayValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || !field.isArray()) {
            return List.of();
        }

        List<String> result = new java.util.ArrayList<>();
        for (JsonNode item : field) {
            result.add(item.asText());
        }
        return result;
    }

    private Map<String, Object> createRequestBody(String prompt) {
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("role", "user");
        content.put("parts", List.of(part));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.2);
        generationConfig.put("maxOutputTokens", 512);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));
        requestBody.put("generationConfig", generationConfig);

        return requestBody;
    }

    public static class EmotionAnalysisResult {
        private final String mainTag;
        private final String emoji;
        private final List<String> keywords;
        private final String interpretation;

        public EmotionAnalysisResult(String mainTag, String emoji, List<String> keywords, String interpretation) {
            this.mainTag = mainTag;
            this.emoji = emoji;
            this.keywords = keywords != null ? keywords : List.of();
            this.interpretation = interpretation;
        }

        public String getMainTag() { return mainTag; }
        public String getEmoji() { return emoji; }
        public List<String> getKeywords() { return keywords; }
        public String getInterpretation() { return interpretation; }
    }

    public static class GeminiAnalysisException extends RuntimeException {
        public GeminiAnalysisException(String message) {
            super(message);
        }

        public GeminiAnalysisException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}