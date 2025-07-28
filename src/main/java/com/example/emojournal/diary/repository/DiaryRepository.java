package com.example.emojournal.diary.repository;

import com.example.emojournal.diary.entity.Diary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    /**
     * 특정 사용자의 일기 목록 조회 (최신순)
     */
    List<Diary> findByUserIdOrderByDiaryDateDesc(String userId);

    /**
     * 특정 사용자의 일기 목록 페이징 조회 (최신순)
     */
    Page<Diary> findByUserIdOrderByDiaryDateDesc(String userId, Pageable pageable);

    /**
     * 특정 사용자의 특정 일기 조회
     */
    Optional<Diary> findByIdAndUserId(Long id, String userId);

    /**
     * 공개된 일기 목록 조회 (최신순)
     */
    Page<Diary> findByIsPublicTrueOrderByDiaryDateDesc(Pageable pageable);

    /**
     * 특정 감정으로 필터링된 일기 목록 조회
     */
    List<Diary> findByUserIdAndAnalyzedEmotionOrderByDiaryDateDesc(String userId, String emotion);

    /**
     * 특정 날짜 범위의 일기 조회
     */
    @Query("SELECT d FROM Diary d WHERE d.userId = :userId AND d.diaryDate BETWEEN :startDate AND :endDate ORDER BY d.diaryDate DESC")
    List<Diary> findByUserIdAndDiaryDateBetween(@Param("userId") String userId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 키워드가 포함된 일기 검색
     */
    @Query("SELECT d FROM Diary d WHERE d.userId = :userId AND " +
            "(LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.emotionKeyword) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.diaryKeywords) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY d.diaryDate DESC")
    List<Diary> searchByKeyword(@Param("userId") String userId, @Param("keyword") String keyword);

    /**
     * 사용자별 감정 통계 조회
     */
    @Query("SELECT d.analyzedEmotion, COUNT(d) FROM Diary d WHERE d.userId = :userId AND d.analyzedEmotion IS NOT NULL GROUP BY d.analyzedEmotion")
    List<Object[]> getEmotionStatistics(@Param("userId") String userId);

    /**
     * 사용자의 최근 N개 일기 조회
     */
    List<Diary> findTop10ByUserIdOrderByDiaryDateDesc(String userId);

    /**
     * 특정 월의 일기 개수 조회
     */
    @Query("SELECT COUNT(d) FROM Diary d WHERE d.userId = :userId AND " +
            "YEAR(d.diaryDate) = :year AND MONTH(d.diaryDate) = :month")
    Long countByUserIdAndYearAndMonth(@Param("userId") String userId,
                                      @Param("year") int year,
                                      @Param("month") int month);

    /**
     * 이미지가 있는 일기 목록 조회
     */
    List<Diary> findByUserIdAndImagePathIsNotNullOrderByDiaryDateDesc(String userId);

    /**
     * 감정 분석이 완료되지 않은 일기 조회 (배치 처리용)
     */
    List<Diary> findByAnalyzedEmotionIsNull();

    /**
     * 사용자별 전체 일기 개수
     */
    Long countByUserId(String userId);

    /**
     * 최근 작성된 일기들 (관리자용)
     */
    List<Diary> findTop20ByOrderByCreatedAtDesc();
}