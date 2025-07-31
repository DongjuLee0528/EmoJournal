package com.example.emojournal.emotion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmotionAnalysisRequest {

    @NotBlank(message = "일기 내용은 필수입니다")
    @Size(min = 1, max = 2000, message = "일기 내용은 1자 이상 2000자 이하여야 합니다")
    private String diaryText;

    private String userId; // 나중에 사용자 인증 추가 시 사용
}