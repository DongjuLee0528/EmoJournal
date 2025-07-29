package com.example.emojournal.emotion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Lombok: getter, setter, toString, equals, hashCode 자동 생성
@NoArgsConstructor // 기본 생성자 생성
@AllArgsConstructor // 모든 필드 값을 인자로 받는 생성자 생성
public class EmotionAnalysisRequest {

    /**
     * 감정 분석 대상인 일기 텍스트
     * - 필수 입력 (@NotBlank)
     * - 1자 이상 2000자 이하 길이 제한 (@Size)
     */
    @NotBlank(message = "일기 내용은 필수입니다")
    @Size(min = 1, max = 2000, message = "일기 내용은 1자 이상 2000자 이하여야 합니다")
    private String diaryText;
}
