package com.example.emojournal.emotion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmotionAnalysisResponse {

    private String originalText;        // 원본 일기 텍스트
    private String emotionTags;         // 전체 감정 해시태그 (예: "#행복 #기쁨 #만족")
    private List<String> tagList;       // 해시태그 리스트 (예: ["행복", "기쁨", "만족"])
    private String mainTag;             // 대표 태그 (예: "#행복")
    private List<String> subTags;       // 서브 태그들 (예: ["#기쁨", "#만족"])
    private String mainEmoji;           // 대표 태그에 어울리는 이모지 (예: "😊")
    private LocalDateTime analyzedAt;   // 분석 시간
    private boolean success;            // 분석 성공 여부
    private String message;             // 응답 메시지

    // 성공 응답 생성용 정적 메서드
    public static EmotionAnalysisResponse success(String originalText, String emotionTags,
                                                  List<String> tagList, String mainTag,
                                                  List<String> subTags, String mainEmoji) {
        EmotionAnalysisResponse response = new EmotionAnalysisResponse();
        response.setOriginalText(originalText);
        response.setEmotionTags(emotionTags);
        response.setTagList(tagList);
        response.setMainTag(mainTag);
        response.setSubTags(subTags);
        response.setMainEmoji(mainEmoji);
        response.setAnalyzedAt(LocalDateTime.now());
        response.setSuccess(true);
        response.setMessage("감정 분석이 완료되었습니다.");
        return response;
    }

    // 실패 응답 생성용 정적 메서드
    public static EmotionAnalysisResponse failure(String originalText, String errorMessage) {
        EmotionAnalysisResponse response = new EmotionAnalysisResponse();
        response.setOriginalText(originalText);
        response.setEmotionTags("#중성");
        response.setTagList(List.of("중성"));
        response.setMainTag("#중성");
        response.setSubTags(List.of());
        response.setMainEmoji("😐");
        response.setAnalyzedAt(LocalDateTime.now());
        response.setSuccess(false);
        response.setMessage(errorMessage);
        return response;
    }
}