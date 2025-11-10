package com.example.emojournal.diary.dto;

import com.example.emojournal.diary.entity.Diary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

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
    private String imagePath;
    private String imageUrl;
    private String originalImageName;

    private String emotionKeyword;
    private String emotionEmoji;
    private String emotionInterpretation;
    private String emotionImageUrl;

    private String userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime diaryDate;
    private Boolean isPublic;
    private Integer viewCount;

    private String summary;
    private boolean hasImage;
    private boolean hasEmotionAnalysis;
    private List<String> allKeywords;

    public static DiaryResponse from(Diary diary) {
        return DiaryResponse.builder()
                .id(diary.getId())
                .title(diary.getTitle())
                .content(diary.getContent())
                .imagePath(diary.getImagePath())
                .originalImageName(diary.getOriginalImageName())
                .emotionKeyword(diary.getAnalyzedEmotion())
                .emotionEmoji(diary.getEmotionEmoji())
                .emotionInterpretation(diary.getEmotionInterpretation())
                .emotionImageUrl(buildEmotionImageUrl(diary.getEmotionImageFile()))
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

    public static DiaryResponse from(Diary diary, String imageAbsoluteUrl) {
        return DiaryResponse.builder()
                .id(diary.getId())
                .title(diary.getTitle())
                .content(diary.getContent())
                .imagePath(diary.getImagePath())
                .imageUrl(imageAbsoluteUrl)
                .originalImageName(diary.getOriginalImageName())
                .emotionKeyword(diary.getAnalyzedEmotion())
                .emotionEmoji(diary.getEmotionEmoji())
                .emotionInterpretation(diary.getEmotionInterpretation())
                .emotionImageUrl(buildEmotionImageUrl(diary.getEmotionImageFile()))
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

    public static DiaryResponse summary(Diary diary) {
        return DiaryResponse.builder()
                .id(diary.getId())
                .title(diary.getTitle())
                .summary(diary.getSummary())
                .emotionKeyword(diary.getAnalyzedEmotion())
                .emotionImageUrl(buildEmotionImageUrl(diary.getEmotionImageFile()))
                .diaryDate(diary.getDiaryDate())
                .createdAt(diary.getCreatedAt())
                .hasImage(diary.hasImage())
                .hasEmotionAnalysis(diary.hasEmotionAnalysis())
                .viewCount(diary.getViewCount())
                .isPublic(diary.getIsPublic())
                .build();
    }

    public static DiaryResponse summary(Diary diary, String imageAbsoluteUrl) {
        return DiaryResponse.builder()
                .id(diary.getId())
                .title(diary.getTitle())
                .summary(diary.getSummary())
                .emotionKeyword(diary.getAnalyzedEmotion())
                .emotionImageUrl(buildEmotionImageUrl(diary.getEmotionImageFile()))
                .imagePath(diary.getImagePath())
                .imageUrl(imageAbsoluteUrl)
                .diaryDate(diary.getDiaryDate())
                .createdAt(diary.getCreatedAt())
                .hasImage(diary.hasImage())
                .hasEmotionAnalysis(diary.hasEmotionAnalysis())
                .viewCount(diary.getViewCount())
                .isPublic(diary.getIsPublic())
                .build();
    }

    private static String buildEmotionImageUrl(String imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }
        String baseUrl = System.getenv("APP_BASE_URL");
        if (baseUrl == null || baseUrl.isEmpty()) {
            return "/images/emotions/" + imageFile;
        }
        return baseUrl + "/images/emotions/" + imageFile;
    }
}