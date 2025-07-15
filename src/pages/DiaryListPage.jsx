// 일기 목록
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
// import { ChevronLeft, ChevronRight } from 'lucide-react';
import Header from '../components/Header';
import BackGround from '../css/background.module.css';
import Footer from '../components/Footer';
import styles from '../css/DiaryListPage.module.css';

const DiaryListPage = () => {
  const navigate = useNavigate();
  const [currentDate, setCurrentDate] = useState({ year: 2025, month: 6 });
  
  // 이미지와 동일한 일기 데이터
  const diaryEntries = [
    { date: '2025.06.30', mood: '좋음' },
    { date: '2025.06.29', mood: '좋음' },
    { date: '2025.06.28', mood: '행복' },
    { date: '2025.06.20', mood: '슬픔' },
    { date: '2025.06.19', mood: '기쁨' },
    { date: '2025.06.16', mood: '좋음' },
    { date: '2025.06.11', mood: '슬픔' },
    { date: '2025.06.7', mood: '좋음' },
    { date: '2025.06.4', mood: '슬픔' },
    { date: '2025.06.1', mood: '슬픔' },
  ];

  const handlePrevMonth = () => {
    if (currentDate.month === 1) {
      setCurrentDate({ year: currentDate.year - 1, month: 12 });
    } else {
      setCurrentDate({ ...currentDate, month: currentDate.month - 1 });
    }
  };

  const handleNextMonth = () => {
    if (currentDate.month === 12) {
      setCurrentDate({ year: currentDate.year + 1, month: 1 });
    } else {
      setCurrentDate({ ...currentDate, month: currentDate.month + 1 });
    }
  };

  const totalEntries = diaryEntries.length;

  return (
    <>
      <Header />
      <div className={styles.container}>
        {/* 상단 헤더 */}
        <div className={styles.headerWrapper}>
          <div className={styles.headerContent}>
            <div className={styles.headerInner}>
              <button 
                onClick={handlePrevMonth}
                className={styles.navButton}
              >
                <span className={styles.navIcon}>◀</span>
              </button>
              
              <div className={styles.headerInfo}>
                <div className={styles.titleText}>
                  내가 쓴 일기들
                </div>
                <div className={styles.dateDisplay}>
                 {currentDate.year}.{String(currentDate.month).padStart(2, '0')}
                </div>
                <div className={styles.countText}>
                  총 {totalEntries}개의 일기
                </div>
              </div>
              
              <button 
                onClick={handleNextMonth}
                className={styles.navButton}
              >
                <span className={styles.navIcon}>▶</span>
              </button>
            </div>
          </div>
        </div>

        {/* 일기 목록 */}
        <div className={styles.listWrapper}>
          <div className={styles.listContent}>
            <div className={styles.listContainer}>
              {diaryEntries.map((entry, index) => (
                <div 
                  key={index}
                  className={styles.diaryItem}
                >
                  <div className={styles.diaryItemInner}>
                    <div className={styles.diaryDate}>
                      {entry.date}
                    </div>
                    <div className={styles.diaryMood}>
                      {entry.mood}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
      <Footer />
    </>
  );
};

export default DiaryListPage;
