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

/**
 * Google Gemini API를 통한 일기 감정 분석 클라이언트
 *
 * 이 클래스는 사용자가 작성한 일기 텍스트를 Google Gemini AI 모델에게 전송하여
 * 감정 분석 결과를 받아오는 기능을 담당합니다.
 *
 * 주요 기능:
 * - 일기 텍스트에서 감정 키워드, 이모지, 해석 추출
 * - Gemini API 호출 및 응답 처리
 * - JSON 형태의 구조화된 감정 분석 결과 반환
 * - API 호출 실패 시 재시도 및 대체 모델 사용
 *
 * @author EmoJournal Team
 * @version 1.0
 */
@Slf4j
@Component
public class GeminiApiClient {

    /** Gemini API 인증 키 */
    @Value("${gemini.api.key}")
    private String apiKey;

    /** Gemini API 기본 URL */
    @Value("${gemini.api.base-url}")
    private String baseUrl;

    /** 사용할 Gemini 모델명 */
    @Value("${gemini.model}")
    private String model;

    /** HTTP 통신을 위한 WebClient 인스턴스 */
    private final WebClient webClient;
    /** JSON 파싱을 위한 ObjectMapper */
    private final ObjectMapper objectMapper;
    /** Markdown 코드 블록 제거를 위한 정규식 패턴 */
    private final Pattern codeBlockPattern = Pattern.compile("^```(json)?\\s*|\\s*```$", Pattern.MULTILINE);

    /**
     * GeminiApiClient 기본 생성자
     * WebClient와 ObjectMapper를 초기화합니다.
     */
    public GeminiApiClient() {
        this.webClient = WebClient.builder()
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 일기 텍스트를 분석하여 감정 분석 결과를 반환합니다.
     *
     * @param diaryText 분석할 일기 텍스트
     * @return 감정 분석 결과 (감정 키워드, 이모지, 해석 등)
     * @throws GeminiAnalysisException 감정 분석 실패 시
     */
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

    /**
     * 기본 모델로 Gemini API를 호출합니다.
     *
     * @param requestBody API 요청 바디
     * @return API 응답 문자열
     */
    private String callGeminiApi(Map<String, Object> requestBody) {
        return callGeminiApiWithModel(requestBody, model);
    }

    /**
     * 지정된 모델로 Gemini API를 호출합니다.
     * 실패 시 재시도 로직과 대체 모델 사용 로직이 포함되어 있습니다.
     *
     * @param requestBody API 요청 바디
     * @param modelToUse 사용할 모델명
     * @return API 응답 문자열
     */
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
                    String responseBody = ex.getResponseBodyAsString();

                    if (statusCode == 403) {
                        log.error("Gemini 응답 코드: {}, 바디: {}", statusCode, responseBody);
                    } else {
                        log.info("상태코드: {}, 모델: {}", statusCode, modelToUse);
                    }

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

    /**
     * Gemini API 응답을 파싱하여 감정 분석 결과로 변환합니다.
     *
     * @param response Gemini API 응답 JSON 문자열
     * @return 파싱된 감정 분석 결과
     * @throws GeminiAnalysisException 파싱 실패 시
     */
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

    /**
     * API 응답에서 유효한 후보를 선택합니다.
     *
     * @param candidates 후보 응답 배열
     * @return 유효한 후보 노드
     * @throws GeminiAnalysisException 유효한 후보가 없을 시
     */
    private JsonNode selectValidCandidate(JsonNode candidates) {
        for (JsonNode candidate : candidates) {
            if (isValidCandidate(candidate)) {
                return candidate;
            }
        }
        throw new GeminiAnalysisException("유효한 후보가 없음");
    }

    /**
     * 후보 응답이 유효한지 검증합니다.
     *
     * @param candidate 검증할 후보 노드
     * @return 유효성 여부
     */
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

    /**
     * 후보 응답에서 텍스트를 추출합니다.
     *
     * @param candidate 텍스트를 추출할 후보 노드
     * @return 추출된 원시 텍스트
     */
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

    /**
     * 원시 텍스트를 정규화합니다.
     * 마크다운 코드 블록과 제어 문자를 제거합니다.
     *
     * @param rawText 정규화할 원시 텍스트
     * @return 정규화된 텍스트
     */
    private String normalizeText(String rawText) {
        if (rawText == null) return "";

        String normalized = rawText.trim();
        normalized = codeBlockPattern.matcher(normalized).replaceAll("");
        normalized = normalized.replaceAll("[\u0000-\u001F\u007F\uFEFF]", "");

        return normalized.trim();
    }

    /**
     * 텍스트에서 균형 잡힌 JSON 블록을 추출합니다.
     *
     * @param text JSON을 포함한 텍스트
     * @return 추출된 JSON 문자열
     * @throws GeminiAnalysisException JSON 형식이 올바르지 않을 시
     */
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

    /**
     * 정규화된 텍스트에서 JSON을 파싱하여 감정 분석 결과로 변환합니다.
     *
     * @param normalizedText 정규화된 응답 텍스트
     * @return 감정 분석 결과
     * @throws GeminiAnalysisException JSON 파싱 실패 시
     */
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

    /**
     * JSON 노드에서 문자열 값을 안전하게 추출합니다.
     *
     * @param node JSON 노드
     * @param fieldName 필드명
     * @return 문자열 값 또는 null
     */
    private String getStringValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull()) ? field.asText() : null;
    }

    /**
     * JSON 노드에서 문자열 배열을 안전하게 추출합니다.
     *
     * @param node JSON 노드
     * @param fieldName 필드명
     * @return 문자열 리스트 (빈 리스트일 수 있음)
     */
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

    /**
     * Gemini API 요청 바디를 생성합니다.
     *
     * @param prompt AI에게 전달할 프롬프트
     * @return API 요청 바디 맵
     */
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

    /**
     * 감정 분석 결과를 담는 불변 클래스
     *
     * @param mainTag 주요 감정 태그
     * @param emoji 감정을 나타내는 이모지
     * @param keywords 연관 키워드 목록
     * @param interpretation 감정 해석 텍스트
     */
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

    /**
     * Gemini 감정 분석 과정에서 발생하는 예외
     */
    public static class GeminiAnalysisException extends RuntimeException {
        public GeminiAnalysisException(String message) {
            super(message);
        }

        public GeminiAnalysisException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}