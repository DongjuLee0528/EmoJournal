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
public class DiaryCreateRequest {

    @Size(max = 100, message = "제목은 100자 이하여야 합니다")
    private String title; // 일기 제목 (선택사항)

    @NotBlank(message = "일기 내용은 필수입니다")
    @Size(min = 1, max = 5000, message = "일기 내용은 1자 이상 5000자 이하여야 합니다")
    private String content; // 일기 내용 (필수)

    private LocalDateTime diaryDate; // 일기 작성 날짜 (선택사항, 기본값은 현재 시간)

    private String userId; // 작성자 ID (추후 인증 시스템에서 자동으로 설정)

    private Boolean isPublic = false; // 공개 여부 (기본값: 비공개)

    // 파일 업로드는 별도 MultipartFile로 처리
    // 감정 분석은 서버에서 자동으로 처리
}