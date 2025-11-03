package com.example.emojournal.diary.service;

import com.example.emojournal.diary.dto.DiaryCreateRequest;
import com.example.emojournal.diary.dto.DiaryResponse;
import com.example.emojournal.diary.dto.DiaryUpdateRequest;
import com.example.emojournal.diary.entity.Diary;
import com.example.emojournal.diary.repository.DiaryRepository;
import com.example.emojournal.emotion.service.EmotionAnalysisService;
import com.example.emojournal.emotion.dto.EmotionAnalysisRequest;
import com.example.emojournal.emotion.dto.EmotionAnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final FileUploadService fileUploadService;
    private final EmotionAnalysisService emotionAnalysisService;

    /**
     * 일기 생성 (파일 업로드 + 감정 분석 포함)
     */
    @Transactional
    public DiaryResponse createDiary(DiaryCreateRequest request, MultipartFile imageFile) {
        try {
            log.info("일기 생성 시작 - 사용자: {}, 내용 길이: {}", request.getUserId(), request.getContent().length());

            Diary diary = Diary.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .userId(request.getUserId())
                    .diaryDate(request.getDiaryDate() != null ? request.getDiaryDate() : LocalDateTime.now())
                    .isPublic(request.getIsPublic())
                    .build();

            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    String uploadedFileName = fileUploadService.uploadFile(imageFile);
                    diary.setImagePath(uploadedFileName);
                    diary.setOriginalImageName(imageFile.getOriginalFilename());
                    log.info("[DIARY_SERVICE] 이미지 업로드 완료 - 원본명: [{}], 저장명: [{}], 크기: {}bytes",
                            imageFile.getOriginalFilename(), uploadedFileName, imageFile.getSize());
                } catch (Exception e) {
                    log.error("[DIARY_SERVICE] 이미지 업로드 실패 - 원본명: [{}], 크기: {}bytes, 오류: {}",
                            imageFile.getOriginalFilename(), imageFile.getSize(), e.getMessage());
                    throw new RuntimeException("이미지 업로드에 실패했습니다: " + e.getMessage(), e);
                }
            } else {
                log.debug("[DIARY_SERVICE] 이미지 파일이 없어 텍스트 전용 일기로 생성");
            }

            diary = diaryRepository.save(diary);
            log.info("[DIARY_SERVICE] 일기 초기 저장 완료 - ID: {}", diary.getId());

            // 감정 분석 수행
            try {
                performEmotionAnalysis(diary);
                diary = diaryRepository.save(diary);
                log.info("[DIARY_SERVICE] 감정 분석 및 최종 저장 완료 - ID: {}, 감정: {}", diary.getId(), diary.getAnalyzedEmotion());
            } catch (Exception e) {
                log.warn("[DIARY_SERVICE] 감정 분석 실패, 기본값 설정 - ID: {}, 오류: {}", diary.getId(), e.getMessage());
                // 감정 분석 실패 시에도 일기 생성은 성공으로 처리
            }

            log.info("[DIARY_SERVICE] 일기 생성 완료 - ID: {}, 감정: {}", diary.getId(), diary.getAnalyzedEmotion());
            return DiaryResponse.from(diary);

        } catch (IllegalArgumentException e) {
            log.error("[DIARY_SERVICE] 유효성 검증 오류 - 사용자: {}, 오류: {}", request.getUserId(), e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.error("[DIARY_SERVICE] 보안 오류 - 사용자: {}, 오류: {}", request.getUserId(), e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            log.error("[DIARY_SERVICE] 런타임 오류 - 사용자: {}, 오류: {}", request.getUserId(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[DIARY_SERVICE] 일기 생성 중 예상치 못한 오류 - 사용자: {}", request.getUserId(), e);
            throw new RuntimeException("일기 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 일기 수정
     */
    @Transactional
    public DiaryResponse updateDiary(Long diaryId, String userId, DiaryUpdateRequest request, MultipartFile imageFile) {
        try {
            log.info("일기 수정 시작 - ID: {}, 사용자: {}", diaryId, userId);

            Diary diary = diaryRepository.findByIdAndUserId(diaryId, userId)
                    .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없거나 수정 권한이 없습니다."));

            diary.setTitle(request.getTitle());
            diary.setContent(request.getContent());
            diary.setDiaryDate(request.getDiaryDate());
            diary.setIsPublic(request.getIsPublic());

            if (Boolean.TRUE.equals(request.getDeleteImage()) && diary.getImagePath() != null) {
                fileUploadService.deleteFile(diary.getImagePath());
                diary.setImagePath(null);
                diary.setOriginalImageName(null);
                log.info("기존 이미지 삭제: {}", diary.getImagePath());
            }

            if (imageFile != null && !imageFile.isEmpty()) {
                if (diary.getImagePath() != null) {
                    fileUploadService.deleteFile(diary.getImagePath());
                }
                String uploadedFileName = fileUploadService.uploadFile(imageFile);
                diary.setImagePath(uploadedFileName);
                diary.setOriginalImageName(imageFile.getOriginalFilename());
                log.info("새 이미지 업로드: {}", uploadedFileName);
            }

            if (Boolean.TRUE.equals(request.getReanalyzeEmotion())) {
                performEmotionAnalysis(diary);
                log.info("감정 재분석 완료: {}", diary.getAnalyzedEmotion());
            }

            diary = diaryRepository.save(diary);
            log.info("일기 수정 완료 - ID: {}", diary.getId());

            return DiaryResponse.from(diary);

        } catch (Exception e) {
            log.error("일기 수정 중 오류 발생 - ID: {}", diaryId, e);
            throw new RuntimeException("일기 수정에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 일기 상세 조회
     */
    @Transactional
    public DiaryResponse getDiary(Long diaryId, String userId) {
        log.info("일기 조회 - ID: {}, 사용자: {}", diaryId, userId);

        Diary diary = diaryRepository.findByIdAndUserId(diaryId, userId)
                .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));

        diary.incrementViewCount();
        diaryRepository.save(diary);

        return DiaryResponse.from(diary);
    }

    /**
     * 일기 목록 조회 (페이징)
     */
    public Page<DiaryResponse> getDiaries(String userId, Pageable pageable) {
        log.info("일기 목록 조회(페이징) - 사용자: {}, 페이지: {}", userId, pageable.getPageNumber());

        Page<Diary> diaries = diaryRepository.findByUserIdOrderByDiaryDateDesc(userId, pageable);
        return diaries.map(DiaryResponse::summary);
    }

    /**
     * 전체 일기 목록 조회
     */
    public List<DiaryResponse> getAllDiaries(String userId) {
        log.info("전체 일기 목록 조회 - 사용자: {}", userId);

        List<Diary> diaries = diaryRepository.findByUserIdOrderByDiaryDateDesc(userId);
        return diaries.stream()
                .map(DiaryResponse::summary)
                .collect(Collectors.toList());
    }

    /**
     * 1년 전 일기 조회
     */
    public DiaryResponse getOneYearAgoDiary(String userId) {
        log.info("1년 전 일기 조회 - 사용자: {}", userId);

        LocalDate targetDate = LocalDate.now().minusYears(1);
        return diaryRepository.findOneYearAgoDiary(userId, targetDate)
                .map(DiaryResponse::from)
                .orElse(null);
    }

    /**
     * 일기 삭제
     */
    @Transactional
    public void deleteDiary(Long diaryId, String userId) {
        log.info("일기 삭제 시작 - ID: {}, 사용자: {}", diaryId, userId);

        Diary diary = diaryRepository.findByIdAndUserId(diaryId, userId)
                .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없거나 삭제 권한이 없습니다."));

        if (diary.getImagePath() != null) {
            fileUploadService.deleteFile(diary.getImagePath());
            log.info("이미지 파일 삭제: {}", diary.getImagePath());
        }

        diaryRepository.delete(diary);
        log.info("일기 삭제 완료 - ID: {}", diaryId);
    }

    /**
     * 키워드 검색
     */
    public List<DiaryResponse> searchDiaries(String userId, String keyword) {
        log.info("일기 검색 - 사용자: {}, 키워드: {}", userId, keyword);

        List<Diary> diaries = diaryRepository.searchByKeyword(userId, keyword);
        return diaries.stream()
                .map(DiaryResponse::summary)
                .collect(Collectors.toList());
    }

    /**
     * 감정별 일기 조회
     */
    public List<DiaryResponse> getDiariesByEmotion(String userId, String emotion) {
        log.info("감정별 일기 조회 - 사용자: {}, 감정: {}", userId, emotion);

        List<Diary> diaries = diaryRepository.findByUserIdAndAnalyzedEmotionOrderByDiaryDateDesc(userId, emotion);
        return diaries.stream()
                .map(DiaryResponse::summary)
                .collect(Collectors.toList());
    }

    /**
     * 감정 통계 조회
     */
    public Map<String, Long> getEmotionStatistics(String userId) {
        log.info("감정 통계 조회 - 사용자: {}", userId);

        List<Object[]> statistics = diaryRepository.getEmotionStatistics(userId);
        return statistics.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }

    /**
     * 감정 분석 실행
     */
    private void performEmotionAnalysis(Diary diary) {
        try {
            log.info("[EMOTION_ANALYSIS] 감정 분석 시작 - 일기 ID: {}, 내용 길이: {}", diary.getId(), diary.getContent().length());

            EmotionAnalysisRequest request = new EmotionAnalysisRequest();
            request.setDiaryText(diary.getContent());

            EmotionAnalysisResponse response = emotionAnalysisService.analyzeEmotion(request);

            if (response.isSuccess()) {
                // 수정된 부분: EmotionAnalysisResponse DTO에 맞게 변경
                diary.setAnalyzedEmotion(response.getMainTag());        // getEmotion() → getMainTag()
                diary.setEmotionKeyword(response.getEmotionTags());     // getEmotionKeyword() → getEmotionTags()
                diary.setDiaryKeywordsList(response.getTagList());      // getDiaryKeywords() → getTagList()
                diary.setEmotionInterpretation(response.getMessage());  // getInterpretation() → getMessage()
                diary.setEmotionImageFile(response.getMainEmoji());     // getImageFileName() → getMainEmoji()

                log.info("[EMOTION_ANALYSIS] 감정 분석 성공 - 일기 ID: {}, 메인 태그: [{}], 전체 태그: [{}], 이모지: {}",
                        diary.getId(), response.getMainTag(), response.getEmotionTags(), response.getMainEmoji());
            } else {
                log.warn("[EMOTION_ANALYSIS] 감정 분석 API 실패 - 일기 ID: {}, 메시지: {}", diary.getId(), response.getMessage());
                setDefaultEmotion(diary);
            }

        } catch (Exception e) {
            log.error("[EMOTION_ANALYSIS] 감정 분석 중 예외 발생 - 일기 ID: {}, 오류 타입: {}, 메시지: {}",
                    diary.getId(), e.getClass().getSimpleName(), e.getMessage());
            setDefaultEmotion(diary);
        }
    }

    /**
     * 감정 분석 실패 시 기본값 설정
     */
    private void setDefaultEmotion(Diary diary) {
        diary.setAnalyzedEmotion("#기쁨");
        diary.setEmotionKeyword("#평온");
        diary.setDiaryKeywordsList(List.of("일반"));
        diary.setEmotionInterpretation("오늘도 소중한 하루였습니다.");
        diary.setEmotionImageFile("😊");
        log.info("[EMOTION_ANALYSIS] 기본 감정 설정 완료 - 일기 ID: {}", diary.getId());
    }
}