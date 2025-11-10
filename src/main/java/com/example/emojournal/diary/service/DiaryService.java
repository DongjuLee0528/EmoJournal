package com.example.emojournal.diary.service;

import com.example.emojournal.diary.dto.DiaryCreateRequest;
import com.example.emojournal.diary.dto.DiaryResponse;
import com.example.emojournal.diary.dto.DiaryUpdateRequest;
import com.example.emojournal.diary.entity.Diary;
import com.example.emojournal.diary.repository.DiaryRepository;
import com.example.emojournal.emotion.service.EmotionAnalysisService;
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
                    log.info("이미지 업로드 완료 - 원본명: [{}], 저장명: [{}], 크기: {}bytes",
                            imageFile.getOriginalFilename(), uploadedFileName, imageFile.getSize());
                } catch (Exception e) {
                    log.error("이미지 업로드 실패 - 원본명: [{}], 크기: {}bytes, 오류: {}",
                            imageFile.getOriginalFilename(), imageFile.getSize(), e.getMessage());
                    throw new RuntimeException("이미지 업로드에 실패했습니다: " + e.getMessage(), e);
                }
            } else {
                log.debug("이미지 파일이 없어 텍스트 전용 일기로 생성");
            }

            diary = diaryRepository.save(diary);
            log.info("일기 초기 저장 완료 - ID: {}", diary.getId());

            performEmotionAnalysis(diary);
            diary = diaryRepository.save(diary);

            log.info("일기 생성 완료 - ID: {}", diary.getId());
            return DiaryResponse.from(diary, fileUploadService.getFileUrl(diary.getImagePath()));

        } catch (IllegalArgumentException e) {
            log.error("유효성 검증 오류 - 사용자: {}, 오류: {}", request.getUserId(), e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.error("보안 오류 - 사용자: {}, 오류: {}", request.getUserId(), e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            log.error("런타임 오류 - 사용자: {}, 오류: {}", request.getUserId(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("일기 생성 중 예상치 못한 오류 - 사용자: {}", request.getUserId(), e);
            throw new RuntimeException("일기 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

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
                log.info("감정 재분석 완료");
            }

            diary = diaryRepository.save(diary);
            log.info("일기 수정 완료 - ID: {}", diary.getId());

            return DiaryResponse.from(diary, fileUploadService.getFileUrl(diary.getImagePath()));

        } catch (Exception e) {
            log.error("일기 수정 중 오류 발생 - ID: {}", diaryId, e);
            throw new RuntimeException("일기 수정에 실패했습니다: " + e.getMessage(), e);
        }
    }

    @Transactional
    public DiaryResponse getDiary(Long diaryId, String userId) {
        log.info("일기 조회 - ID: {}, 사용자: {}", diaryId, userId);

        Diary diary = diaryRepository.findByIdAndUserId(diaryId, userId)
                .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));

        diary.incrementViewCount();
        diaryRepository.save(diary);

        return DiaryResponse.from(diary, fileUploadService.getFileUrl(diary.getImagePath()));
    }

    public Page<DiaryResponse> getDiaries(String userId, Pageable pageable) {
        log.info("일기 목록 조회(페이징) - 사용자: {}, 페이지: {}", userId, pageable.getPageNumber());

        Page<Diary> diaries = diaryRepository.findByUserIdOrderByDiaryDateDesc(userId, pageable);
        return diaries.map(diary -> DiaryResponse.summary(diary, fileUploadService.getFileUrl(diary.getImagePath())));
    }

    public List<DiaryResponse> getAllDiaries(String userId) {
        log.info("전체 일기 목록 조회 - 사용자: {}", userId);

        List<Diary> diaries = diaryRepository.findByUserIdOrderByDiaryDateDesc(userId);
        return diaries.stream()
                .map(diary -> DiaryResponse.summary(diary, fileUploadService.getFileUrl(diary.getImagePath())))
                .collect(Collectors.toList());
    }

    public DiaryResponse getOneYearAgoDiary(String userId) {
        log.info("1년 전 일기 조회 - 사용자: {}", userId);

        LocalDate targetDate = LocalDate.now().minusYears(1);
        return diaryRepository.findOneYearAgoDiary(userId, targetDate)
                .map(diary -> DiaryResponse.from(diary, fileUploadService.getFileUrl(diary.getImagePath())))
                .orElse(null);
    }

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

    public List<DiaryResponse> searchDiaries(String userId, String keyword) {
        log.info("일기 검색 - 사용자: {}, 키워드: {}", userId, keyword);

        List<Diary> diaries = diaryRepository.searchByKeyword(userId, keyword);
        return diaries.stream()
                .map(diary -> DiaryResponse.summary(diary, fileUploadService.getFileUrl(diary.getImagePath())))
                .collect(Collectors.toList());
    }

    public List<DiaryResponse> getDiariesByEmotion(String userId, String emotion) {
        log.info("감정별 일기 조회 - 사용자: {}, 감정: {}", userId, emotion);

        List<Diary> diaries = diaryRepository.findByUserIdAndAnalyzedEmotionOrderByDiaryDateDesc(userId, emotion);
        return diaries.stream()
                .map(diary -> DiaryResponse.summary(diary, fileUploadService.getFileUrl(diary.getImagePath())))
                .collect(Collectors.toList());
    }

    public Map<String, Long> getEmotionStatistics(String userId) {
        log.info("감정 통계 조회 - 사용자: {}", userId);

        List<Object[]> statistics = diaryRepository.getEmotionStatistics(userId);
        return statistics.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }

    private void performEmotionAnalysis(Diary diary) {
        try {
            log.info("감정 분석 시작 - 일기 ID: {}", diary.getId());

            EmotionAnalysisService.EmotionAnalysisResult result = emotionAnalysisService.analyzeEmotion(diary.getContent());

            if (result != null) {
                diary.setAnalyzedEmotion(result.getMainTag());
                diary.setEmotionKeyword(String.join(", ", result.getSubTags()));
                diary.setEmotionImageFile(result.getImageFile());
                diary.setEmotionInterpretation(result.getInterpretation());

                log.info("감정 분석 성공 - 일기 ID: {}, 감정: {}", diary.getId(), result.getMainTag());
            } else {
                log.warn("감정 분석 실패 - 일기 ID: {}", diary.getId());
            }

        } catch (Exception e) {
            log.error("감정 분석 중 오류 - 일기 ID: {}, 오류: {}", diary.getId(), e.getMessage());
        }
    }
}