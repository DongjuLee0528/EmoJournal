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

    List<Diary> findByUserIdOrderByDiaryDateDesc(String userId);

    Page<Diary> findByUserIdOrderByDiaryDateDesc(String userId, Pageable pageable);

    Optional<Diary> findByIdAndUserId(Long id, String userId);

    Page<Diary> findByIsPublicTrueOrderByDiaryDateDesc(Pageable pageable);

    List<Diary> findByUserIdAndAnalyzedEmotionOrderByDiaryDateDesc(String userId, String emotion);

    @Query("SELECT d FROM Diary d WHERE d.userId = :userId AND d.diaryDate BETWEEN :startDate AND :endDate ORDER BY d.diaryDate DESC")
    List<Diary> findByUserIdAndDiaryDateBetween(@Param("userId") String userId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT d FROM Diary d WHERE d.userId = :userId AND " +
            "(LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.emotionKeyword) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.diaryKeywords) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY d.diaryDate DESC")
    List<Diary> searchByKeyword(@Param("userId") String userId, @Param("keyword") String keyword);

    @Query("SELECT d.analyzedEmotion, COUNT(d) FROM Diary d WHERE d.userId = :userId AND d.analyzedEmotion IS NOT NULL GROUP BY d.analyzedEmotion")
    List<Object[]> getEmotionStatistics(@Param("userId") String userId);

    List<Diary> findTop10ByUserIdOrderByDiaryDateDesc(String userId);

    @Query("SELECT COUNT(d) FROM Diary d WHERE d.userId = :userId AND " +
            "YEAR(d.diaryDate) = :year AND MONTH(d.diaryDate) = :month")
    Long countByUserIdAndYearAndMonth(@Param("userId") String userId,
                                      @Param("year") int year,
                                      @Param("month") int month);

    List<Diary> findByUserIdAndImagePathIsNotNullOrderByDiaryDateDesc(String userId);

    List<Diary> findByAnalyzedEmotionIsNull();

    Long countByUserId(String userId);

    List<Diary> findTop20ByOrderByCreatedAtDesc();

    @Query("SELECT d FROM Diary d WHERE d.userId = :userId " +
            "AND FUNCTION('DATE', d.diaryDate) = :targetDate")
    Optional<Diary> findOneYearAgoDiary(@Param("userId") String userId,
                                        @Param("targetDate") LocalDate targetDate);

    List<Diary> findAllByUserIdOrderByDiaryDateDesc(String userId);

    @Query("SELECT d FROM Diary d WHERE d.userId = :userId AND FUNCTION('DATE', d.diaryDate) = :targetDate ORDER BY d.diaryDate DESC")
    List<Diary> findByUserIdAndDiaryDate(@Param("userId") String userId, @Param("targetDate") LocalDate targetDate);
}
