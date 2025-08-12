package com.example.emojournal.emotion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmotionAnalysisResponse {

    // 새로운 구조의 필드들
    private String originalText;        // 원본 일기 텍스트
    private String emotion;             // 대표 감정 (9가지 중 1개)
    private String emotionKeyword;      // 감정 키워드 (1개)
    private List<String> diaryKeywords; // 일기 키워드 (1~2개)
    private String imageFileName;       // 감정 이미지 파일명
    private String interpretation;      // 감정 해석
    private LocalDateTime analyzedAt;   // 분석 시간
    private boolean success;            // 분석 성공 여부
    private String message;             // 응답 메시지

    // 하위 호환성을 위한 기존 필드들 (deprecated)
    @Deprecated
    private List<String> keywords;      // 기존 키워드 (emotionKeyword + diaryKeywords 합친 것)
    @Deprecated
    private String emoji;               // 기존 이모지
    @Deprecated
    private String emotionTags;         // 기존 해시태그 방식
    @Deprecated
    private List<String> tagList;       // 기존 태그 리스트
    @Deprecated
    private String mainTag;             // 기존 대표 태그
    @Deprecated
    private List<String> subTags;       // 기존 서브 태그
    @Deprecated
    private String mainEmoji;           // 기존 이모지

    // 성공 응답 생성용 정적 메서드 (새 버전)
    public static EmotionAnalysisResponse success(String originalText, String emotion,
                                                  String emotionKeyword, List<String> diaryKeywords,
                                                  String imageFileName, String interpretation) {
        EmotionAnalysisResponse response = new EmotionAnalysisResponse();
        response.setOriginalText(originalText);
        response.setEmotion(emotion);
        response.setEmotionKeyword(emotionKeyword);
        response.setDiaryKeywords(diaryKeywords);
        response.setImageFileName(imageFileName);
        response.setInterpretation(interpretation);
        response.setAnalyzedAt(LocalDateTime.now());
        response.setSuccess(true);
        response.setMessage("감정 분석이 완료되었습니다.");

        // 하위 호환성을 위한 필드 설정
        List<String> allKeywords = new java.util.ArrayList<>();
        allKeywords.add(emotionKeyword);
        allKeywords.addAll(diaryKeywords);
        response.setKeywords(allKeywords);

        // 기존 이모지 설정
        String emoji = getEmotionEmoji(emotion);
        response.setEmoji(emoji);
        response.setMainEmoji(emoji);

        response.setEmotionTags("#" + emotion);
        response.setTagList(List.of(emotion));
        response.setMainTag("#" + emotion);
        response.setSubTags(diaryKeywords.stream().map(k -> "#" + k).toList());

        return response;
    }

    // 기존 성공 응답 생성 메서드 (하위 호환성)
    @Deprecated
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

        // 새 필드들에도 값 설정
        response.setEmotion(mainTag.replace("#", ""));
        response.setEmotionKeyword("일반");
        response.setDiaryKeywords(subTags.stream().map(tag -> tag.replace("#", "")).toList());
        response.setImageFileName(getImageFileNameFromEmotion(mainTag.replace("#", "")));
        response.setKeywords(subTags.stream().map(tag -> tag.replace("#", "")).toList());
        response.setEmoji(mainEmoji);
        response.setInterpretation("감정 분석이 완료되었습니다.");

        return response;
    }

    // 실패 응답 생성용 정적 메서드
    public static EmotionAnalysisResponse failure(String originalText, String errorMessage) {
        EmotionAnalysisResponse response = new EmotionAnalysisResponse();
        response.setOriginalText(originalText);
        response.setEmotion("기쁨");
        response.setEmotionKeyword("평온");
        response.setDiaryKeywords(List.of("일반"));
        response.setImageFileName("joy.png");
        response.setInterpretation("감정 분석에 실패했습니다.");
        response.setAnalyzedAt(LocalDateTime.now());
        response.setSuccess(false);
        response.setMessage(errorMessage);

        // 하위 호환성을 위한 기본값 설정
        response.setKeywords(List.of("평온", "일반"));
        response.setEmoji("😊");
        response.setEmotionTags("#기쁨");
        response.setTagList(List.of("기쁨"));
        response.setMainTag("#기쁨");
        response.setSubTags(List.of("#일반"));
        response.setMainEmoji("😊");

        return response;
    }

    // 감정별 이모지 반환 (하위 호환성용)
    private static String getEmotionEmoji(String emotion) {
        Map<String, String> emotionEmojiMap = Map.of(
                "기쁨", "😊", "슬픔", "😢", "분노", "😠", "두려움", "😰",
                "혐오감", "🤢", "놀람", "😲", "신뢰감", "🤝", "사랑", "❤️", "혼합감정", "😐"
        );
        return emotionEmojiMap.getOrDefault(emotion, "😐");
    }

    // 감정별 이미지 파일명 반환 (하위 호환성용)
    private static String getImageFileNameFromEmotion(String emotion) {
        Map<String, String> emotionImageMap = Map.of(
                "기쁨", "joy.png", "슬픔", "sadness.png", "분노", "anger.png", "두려움", "fear.png",
                "혐오감", "disgust.png", "놀람", "surprise.png", "신뢰감", "trust.png",
                "사랑", "love.png", "혼합감정", "mixed.png"
        );
        return emotionImageMap.getOrDefault(emotion, "joy.png");
    }

    // 새로운 응답 데이터 구조 확인용 메서드
    public boolean hasNewStructure() {
        return emotion != null && emotionKeyword != null && diaryKeywords != null && interpretation != null;
    }

    // 감정 분석 결과 요약 정보
    public String getSummary() {
        if (success) {
            return String.format("감정: %s, 감정키워드: %s, 일기키워드: %s",
                    emotion,
                    emotionKeyword,
                    String.join(", ", diaryKeywords)
            );
        } else {
            return "분석 실패: " + message;
        }
    }

    // 전체 키워드 리스트 반환 (편의 메서드)
    public List<String> getAllKeywords() {
        if (emotionKeyword == null || diaryKeywords == null) {
            return keywords != null ? keywords : List.of();
        }

        List<String> allKeywords = new java.util.ArrayList<>();
        allKeywords.add(emotionKeyword);
        allKeywords.addAll(diaryKeywords);
        return allKeywords;
    }
}