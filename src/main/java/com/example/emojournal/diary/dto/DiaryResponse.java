package com.example.emojournal.diary.dto;

import com.example.emojournal.diary.entity.Diary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryResponse {

    private Long id;
    private String title;
    private String content;
    private String imagePath; // 업로드된 이미지 경로 (null이면 이미지 없음)
    private String originalImageName; // 원본 파일명

    // 감정 분석 결과
    private String analyzedEmotion; // 분석된 감정
    private String emotionKeyword; // 감정 키워드
    private List<String> diaryKeywords; // 일기 키워드들
    private String emotionInterpretation; // 감정 해석
    private String emotionImageUrl; // 감정별 고양이 이미지 URL

    // 메타 정보
    private String userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime diaryDate;
    private Boolean isPublic;
    private Integer viewCount;

    // 편의 필드들
    private String summary; // 일기 요약
    private boolean hasImage; // 이미지 포함 여부
    private boolean hasEmotionAnalysis; // 감정 분석 완료 여부
    private List<String> allKeywords; // 전체 키워드 (감정키워드 + 일기키워드)

    /**
     * Entity를 DTO로 변환하는 정적 메서드
     */
    public static DiaryResponse from(Diary diary) {
        return DiaryResponse.builder()
                .id(diary.getId())
                .title(diary.getTitle())
                .content(diary.getContent())
                .imagePath(diary.getImagePath())
                .originalImageName(diary.getOriginalImageName())
                .analyzedEmotion(diary.getAnalyzedEmotion())
                .emotionKeyword(diary.getEmotionKeyword())
                .diaryKeywords(diary.getDiaryKeywordsList())
                .emotionInterpretation(diary.getEmotionInterpretation())
                .emotionImageUrl(diary.getEmotionImageUrl())
                .userId(diary.getUserId())
                .createdAt(diary.getCreatedAt())
                .updatedAt(diary.getUpdatedAt())
                .diaryDate(diary.getDiaryDate())
                .isPublic(diary.getIsPublic())
                .viewCount(diary.getViewCount())
                .summary(diary.getSummary())
                .hasImage(diary.hasImage())
                .hasEmotionAnalysis(diary.hasEmotionAnalysis())
                .allKeywords(diary.getAllKeywords())
                .build();
    }

    /**
     * 요약 정보만 포함하는 간단한 응답 생성 (목록 조회용)
     */
    public static DiaryResponse summary(Diary diary) {
        return DiaryResponse.builder()
                .id(diary.getId())
                .title(diary.getTitle())
                .summary(diary.getSummary())
                .analyzedEmotion(diary.getAnalyzedEmotion())
                .emotionImageUrl(diary.getEmotionImageUrl())
                .diaryDate(diary.getDiaryDate())
                .createdAt(diary.getCreatedAt())
                .hasImage(diary.hasImage())
                .hasEmotionAnalysis(diary.hasEmotionAnalysis())
                .viewCount(diary.getViewCount())
                .isPublic(diary.getIsPublic())
                .build();
    }
}