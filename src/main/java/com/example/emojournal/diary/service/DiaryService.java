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
                String uploadedFileName = fileUploadService.uploadFile(imageFile);
                diary.setImagePath(uploadedFileName);
                diary.setOriginalImageName(imageFile.getOriginalFilename());
                log.info("이미지 업로드 완료: {}", uploadedFileName);
            }

            diary = diaryRepository.save(diary);
            performEmotionAnalysis(diary);
            diary = diaryRepository.save(diary);

            log.info("일기 생성 완료 - ID: {}, 감정: {}", diary.getId(), diary.getAnalyzedEmotion());
            return DiaryResponse.from(diary);

        } catch (Exception e) {
            log.error("일기 생성 중 오류 발생", e);
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
            log.debug("감정 분석 시작 - 일기 ID: {}", diary.getId());

            EmotionAnalysisRequest request = new EmotionAnalysisRequest();
            request.setDiaryText(diary.getContent());

            EmotionAnalysisResponse response = emotionAnalysisService.analyzeEmotion(request);

            if (response.isSuccess()) {
                diary.setAnalyzedEmotion(response.getEmotion());
                diary.setEmotionKeyword(response.getEmotionKeyword());
                diary.setDiaryKeywordsList(response.getDiaryKeywords());
                diary.setEmotionInterpretation(response.getInterpretation());
                diary.setEmotionImageFile(response.getImageFileName());

                log.debug("감정 분석 성공 - 감정: {}, 키워드: {}", response.getEmotion(), response.getAllKeywords());
            } else {
                log.warn("감정 분석 실패 - ID: {}, 메시지: {}", diary.getId(), response.getMessage());
                setDefaultEmotion(diary);
            }

        } catch (Exception e) {
            log.error("감정 분석 오류 - ID: {}", diary.getId(), e);
            setDefaultEmotion(diary);
        }
    }

    /**
     * 감정 분석 실패 시 기본값 설정
     */
    private void setDefaultEmotion(Diary diary) {
        diary.setAnalyzedEmotion("기쁨");
        diary.setEmotionKeyword("평온");
        diary.setDiaryKeywordsList(List.of("일반"));
        diary.setEmotionInterpretation("오늘도 소중한 하루였습니다.");
        diary.setEmotionImageFile("joy.png");
    }
}
