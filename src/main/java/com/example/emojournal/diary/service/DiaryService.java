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

/**
 * 일기 비즈니스 로직 서비스
 *
 * 일기의 CRUD 작업과 감정 분석, 파일 업로드 등의 비즈니스 로직을 담당합니다.
 * 데이터베이스 트랜잭션 관리를 통해 데이터 일관성을 보장합니다.
 *
 * 주요 기능:
 * - 일기 생성 및 감정 분석 자동 수행
 * - 이미지 파일 업로드 및 관리
 * - 일기 수정, 삭제 및 조회
 * - 키워드 검색 및 감정별 필터링
 * - 감정 통계 생성
 * - 조회수 자동 증가
 *
 * @author EmoJournal Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {

    /** 일기 데이터 액세스 레이어 */
    private final DiaryRepository diaryRepository;
    /** 파일 업로드 처리 서비스 */
    private final FileUploadService fileUploadService;
    /** AI 기반 감정 분석 서비스 */
    private final EmotionAnalysisService emotionAnalysisService;

    /**
     * 새로운 일기를 생성하고 감정 분석을 수행합니다.
     * 이미지 파일이 있는 경우 업로드를 수행하고, 일기 내용에 대해 AI 감정 분석을 자동으로 수행합니다.
     *
     * @param request 일기 생성 요청 데이터
     * @param imageFile 첨부할 이미지 파일 (선택사항)
     * @return 생성된 일기 정보와 감정 분석 결과
     * @throws RuntimeException 일기 생성 실패 시
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

    /**
     * 기존 일기를 수정합니다.
     * 텍스트 내용, 이미지 파일 변경과 감정 재분석 옵션을 지원합니다.
     * 기존 이미지 삭제나 새 이미지 교체도 가능합니다.
     *
     * @param diaryId 수정할 일기 ID
     * @param userId 사용자 ID (권한 검증용)
     * @param request 일기 수정 요청 데이터
     * @param imageFile 새로 첨부할 이미지 파일 (선택사항)
     * @return 수정된 일기 정보
     * @throws RuntimeException 일기 수정 실패 시
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

    /**
     * 특정 일기를 조회하고 조회수를 증가시킵니다.
     * 사용자의 소유 일기만 조회 가능하며, 조회 시 자동으로 viewCount가 1 증가합니다.
     *
     * @param diaryId 조회할 일기 ID
     * @param userId 사용자 ID (권한 검증용)
     * @return 일기 상세 정보
     * @throws IllegalArgumentException 일기를 찾을 수 없거나 권한이 없는 경우
     */
    @Transactional
    public DiaryResponse getDiary(Long diaryId, String userId) {
        log.info("일기 조회 - ID: {}, 사용자: {}", diaryId, userId);

        Diary diary = diaryRepository.findByIdAndUserId(diaryId, userId)
                .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));

        diary.incrementViewCount();
        diaryRepository.save(diary);

        return DiaryResponse.from(diary, fileUploadService.getFileUrl(diary.getImagePath()));
    }

    /**
     * 사용자의 일기 목록을 페이징하여 조회합니다.
     * 최신 작성일 순으로 정렬되어 반환되며, 각 일기는 요약 정보로 제공됩니다.
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보 (페이지, 크기, 정렬 조건)
     * @return 페이징된 일기 목록
     */
    public Page<DiaryResponse> getDiaries(String userId, Pageable pageable) {
        log.info("일기 목록 조회(페이징) - 사용자: {}, 페이지: {}", userId, pageable.getPageNumber());

        Page<Diary> diaries = diaryRepository.findByUserIdOrderByDiaryDateDesc(userId, pageable);
        return diaries.map(diary -> DiaryResponse.summary(diary, fileUploadService.getFileUrl(diary.getImagePath())));
    }

    /**
     * 사용자의 모든 일기를 조회합니다.
     * 페이징 없이 전체 일기를 반환하므로 대용량 데이터 시 성능에 주의가 필요합니다.
     *
     * @param userId 사용자 ID
     * @return 전체 일기 목록 (요약 정보)
     */
    public List<DiaryResponse> getAllDiaries(String userId) {
        log.info("전체 일기 목록 조회 - 사용자: {}", userId);

        List<Diary> diaries = diaryRepository.findByUserIdOrderByDiaryDateDesc(userId);
        return diaries.stream()
                .map(diary -> DiaryResponse.summary(diary, fileUploadService.getFileUrl(diary.getImagePath())))
                .collect(Collectors.toList());
    }

    /**
     * 1년 전 오늘 작성된 일기를 조회합니다.
     * 추억을 되돌아보는 기능으로, 정확히 1년 전 같은 날짜에 작성된 일기를 찾습니다.
     *
     * @param userId 사용자 ID
     * @return 1년 전 오늘의 일기 (없으면 null)
     */
    public DiaryResponse getOneYearAgoDiary(String userId) {
        log.info("1년 전 일기 조회 - 사용자: {}", userId);

        LocalDate targetDate = LocalDate.now().minusYears(1);
        return diaryRepository.findOneYearAgoDiary(userId, targetDate)
                .map(diary -> DiaryResponse.from(diary, fileUploadService.getFileUrl(diary.getImagePath())))
                .orElse(null);
    }

    /**
     * 특정 일기를 삭제합니다.
     * 일기와 함께 첨부된 이미지 파일도 완전히 삭제됩니다.
     *
     * @param diaryId 삭제할 일기 ID
     * @param userId 사용자 ID (권한 검증용)
     * @throws IllegalArgumentException 일기를 찾을 수 없거나 권한이 없는 경우
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
     * 키워드로 일기를 검색합니다.
     * 일기의 제목과 내용에서 키워드를 포함하는 일기들을 검색합니다.
     *
     * @param userId 사용자 ID
     * @param keyword 검색할 키워드
     * @return 검색된 일기 목록 (요약 정보)
     */
    public List<DiaryResponse> searchDiaries(String userId, String keyword) {
        log.info("일기 검색 - 사용자: {}, 키워드: {}", userId, keyword);

        List<Diary> diaries = diaryRepository.searchByKeyword(userId, keyword);
        return diaries.stream()
                .map(diary -> DiaryResponse.summary(diary, fileUploadService.getFileUrl(diary.getImagePath())))
                .collect(Collectors.toList());
    }

    /**
     * 특정 감정으로 분류된 일기들을 조회합니다.
     * AI 감정 분석 결과를 기반으로 필터링하여 반환합니다.
     *
     * @param userId 사용자 ID
     * @param emotion 검색할 감정 카테고리
     * @return 해당 감정의 일기 목록 (최신 순)
     */
    public List<DiaryResponse> getDiariesByEmotion(String userId, String emotion) {
        log.info("감정별 일기 조회 - 사용자: {}, 감정: {}", userId, emotion);

        List<Diary> diaries = diaryRepository.findByUserIdAndAnalyzedEmotionOrderByDiaryDateDesc(userId, emotion);
        return diaries.stream()
                .map(diary -> DiaryResponse.summary(diary, fileUploadService.getFileUrl(diary.getImagePath())))
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 감정별 일기 작성 통계를 조회합니다.
     * 각 감정 카테고리별로 작성된 일기의 개수를 집계하여 반환합니다.
     *
     * @param userId 사용자 ID
     * @return 감정별 일기 개수 맵
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
     * 일기에 대한 AI 감정 분석을 수행하고 결과를 저장합니다.
     * 분석이 성공하면 일기 엔티티에 감정 정보를 업데이트합니다.
     * 분석 실패 시에도 예외를 발생시키지 않고 로그만 기록합니다.
     *
     * @param diary 감정 분석을 수행할 일기 엔티티
     */
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