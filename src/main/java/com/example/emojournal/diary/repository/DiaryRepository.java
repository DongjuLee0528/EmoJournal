package com.example.emojournal.diary.repository;

import com.example.emojournal.diary.entity.Diary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    /**
     * 특정 사용자의 모든 일기를 최신순으로 조회
     */
    List<Diary> findByUserIdOrderByDiaryDateDesc(String userId);

    /**
     * 특정 사용자의 일기 목록을 페이지 단위로 최신순 조회
     */
    Page<Diary> findByUserIdOrderByDiaryDateDesc(String userId, Pageable pageable);

    /**
     * 사용자 ID와 일기 ID로 특정 일기 단건 조회
     */
    Optional<Diary> findByIdAndUserId(Long id, String userId);

    /**
     * 공개된 모든 일기 목록을 페이지 단위로 최신순 조회
     */
    Page<Diary> findByIsPublicTrueOrderByDiaryDateDesc(Pageable pageable);

    /**
     * 특정 사용자의 특정 감정 일기만 필터링해서 최신순으로 조회
     */
    List<Diary> findByUserIdAndAnalyzedEmotionOrderByDiaryDateDesc(String userId, String emotion);

    /**
     * 특정 날짜 범위(start ~ end) 내 작성된 일기 조회 (최신순)
     */
    @Query("SELECT d FROM Diary d WHERE d.userId = :userId AND d.diaryDate BETWEEN :startDate AND :endDate ORDER BY d.diaryDate DESC")
    List<Diary> findByUserIdAndDiaryDateBetween(@Param("userId") String userId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    /**
     * 일기 제목/내용/감정키워드/일기키워드 내 검색어(keyword)가 포함된 일기 검색
     */
    @Query("SELECT d FROM Diary d WHERE d.userId = :userId AND " +
            "(LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.emotionKeyword) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.diaryKeywords) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY d.diaryDate DESC")
    List<Diary> searchByKeyword(@Param("userId") String userId, @Param("keyword") String keyword);

    /**
     * 사용자별 감정 통계 데이터 (감정별 일기 개수 집계)
     */
    @Query("SELECT d.analyzedEmotion, COUNT(d) FROM Diary d WHERE d.userId = :userId AND d.analyzedEmotion IS NOT NULL GROUP BY d.analyzedEmotion")
    List<Object[]> getEmotionStatistics(@Param("userId") String userId);

    /**
     * 최근 10개의 일기만 조회 (홈화면 요약용 등)
     */
    List<Diary> findTop10ByUserIdOrderByDiaryDateDesc(String userId);

    /**
     * 특정 연/월에 해당하는 일기 개수 조회 (예: 월별 통계)
     */
    @Query("SELECT COUNT(d) FROM Diary d WHERE d.userId = :userId AND " +
            "YEAR(d.diaryDate) = :year AND MONTH(d.diaryDate) = :month")
    Long countByUserIdAndYearAndMonth(@Param("userId") String userId,
                                      @Param("year") int year,
                                      @Param("month") int month);

    /**
     * 이미지가 첨부된 일기만 최신순으로 조회
     */
    List<Diary> findByUserIdAndImagePathIsNotNullOrderByDiaryDateDesc(String userId);

    /**
     * 감정 분석이 아직 처리되지 않은 일기 목록 조회 (배치 처리용)
     */
    List<Diary> findByAnalyzedEmotionIsNull();

    /**
     * 사용자의 전체 일기 개수 조회
     */
    Long countByUserId(String userId);

    /**
     * 최근 작성된 전체 일기 20개 조회 (관리자 대시보드용 등)
     */
    List<Diary> findTop20ByOrderByCreatedAtDesc();

    /**
     * 1년 전 오늘 작성한 일기 조회
     */
    @Query("SELECT d FROM Diary d WHERE d.userId = :userId " +
            "AND FUNCTION('DATE', d.diaryDate) = :targetDate")
    Optional<Diary> findOneYearAgoDiary(@Param("userId") String userId,
                                        @Param("targetDate") LocalDate targetDate);

    /**
     * 특정 사용자의 모든 일기 목록 조회 (최신순)
     */
    List<Diary> findAllByUserIdOrderByDiaryDateDesc(String userId);

    /**
     * 특정 날짜의 일기 조회 (예: 2025-06-30)
     */
    @Query("SELECT d FROM Diary d WHERE d.userId = :userId AND FUNCTION('DATE', d.diaryDate) = :targetDate ORDER BY d.diaryDate DESC")
    List<Diary> findByUserIdAndDiaryDate(@Param("userId") String userId, @Param("targetDate") LocalDate targetDate);
}
