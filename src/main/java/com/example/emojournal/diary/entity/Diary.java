package com.example.emojournal.diary.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "diaries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String title; // 일기 제목 (선택사항)

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 일기 내용 (필수)

    @Column(name = "image_path")
    private String imagePath; // 업로드된 사진 파일 경로

    @Column(name = "original_image_name")
    private String originalImageName; // 원본 파일명

    // 감정 분석 결과 필드들
    @Column(name = "analyzed_emotion", length = 20)
    private String analyzedEmotion; // 분석된 감정 (9가지 중 1개)

    @Column(name = "emotion_keyword", length = 50)
    private String emotionKeyword; // 감정 키워드 (1개)

    @Column(name = "diary_keywords", length = 200)
    private String diaryKeywords; // 일기 키워드들 (쉼표로 구분된 문자열)

    @Column(name = "emotion_interpretation", columnDefinition = "TEXT")
    private String emotionInterpretation; // 감정 해석

    @Column(name = "emotion_image_file")
    private String emotionImageFile; // 감정별 고양이 이미지 파일명

    // 작성자 정보 (추후 사용자 시스템 추가시)
    @Column(name = "user_id", length = 50)
    private String userId;

    // 시간 정보
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "diary_date")
    private LocalDateTime diaryDate; // 일기 작성 날짜 (사용자가 선택 가능)

    // 추가 메타데이터
    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = false; // 공개 여부

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0; // 조회수

    // 편의 메서드들

    /**
     * 일기 키워드 문자열을 리스트로 변환
     */
    public List<String> getDiaryKeywordsList() {
        if (diaryKeywords == null || diaryKeywords.trim().isEmpty()) {
            return List.of();
        }
        return List.of(diaryKeywords.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /**
     * 키워드 리스트를 문자열로 변환하여 저장
     */
    public void setDiaryKeywordsList(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            this.diaryKeywords = null;
        } else {
            this.diaryKeywords = String.join(",", keywords);
        }
    }

    /**
     * 전체 키워드 리스트 반환 (감정키워드 + 일기키워드)
     */
    public List<String> getAllKeywords() {
        List<String> allKeywords = new java.util.ArrayList<>();
        if (emotionKeyword != null && !emotionKeyword.trim().isEmpty()) {
            allKeywords.add(emotionKeyword);
        }
        allKeywords.addAll(getDiaryKeywordsList());
        return allKeywords;
    }

    /**
     * 감정 이미지 URL 반환
     */
    public String getEmotionImageUrl() {
        if (emotionImageFile == null || emotionImageFile.trim().isEmpty()) {
            return "/images/emotions/joy.png"; // 기본 이미지
        }
        return "/images/emotions/" + emotionImageFile;
    }

    /**
     * 일기 요약 정보 반환 (제목 또는 내용 일부)
     */
    public String getSummary() {
        if (title != null && !title.trim().isEmpty()) {
            return title;
        }
        if (content != null && content.length() > 50) {
            return content.substring(0, 50) + "...";
        }
        return content;
    }

    /**
     * 사진 업로드 여부 확인
     */
    public boolean hasImage() {
        return imagePath != null && !imagePath.trim().isEmpty();
    }

    /**
     * 감정 분석 완료 여부 확인
     */
    public boolean hasEmotionAnalysis() {
        return analyzedEmotion != null && !analyzedEmotion.trim().isEmpty();
    }

    /**
     * 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null) ? 1 : this.viewCount + 1;
    }
}