package com.example.emojournal.diary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaryUpdateRequest {

    @Size(max = 100, message = "제목은 100자 이하여야 합니다")
    private String title; // 일기 제목

    @NotBlank(message = "일기 내용은 필수입니다")
    @Size(min = 1, max = 5000, message = "일기 내용은 1자 이상 5000자 이하여야 합니다")
    private String content; // 일기 내용

    private LocalDateTime diaryDate; // 일기 작성 날짜

    private Boolean isPublic; // 공개 여부

    private Boolean deleteImage = false; // 기존 이미지 삭제 여부

    private Boolean reanalyzeEmotion = false; // 감정 재분석 여부 (내용 수정시)
}