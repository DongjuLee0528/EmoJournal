import React, { useState, useMemo } from 'react';
import styled from 'styled-components';
import Header from '../components/Header';
import Footer from '../components/Footer';

const Container = styled.div`
  font-family: '온글잎 의연체', sans-serif;
  width: 97%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  max-width: 1920px;
  max-height: 1010px;
  /* margin-top : 2rem; */
  /* margin-bottom:2rem; */
`;

const HeaderWrapper = styled.div`
  width: 100%;
  max-width: 64rem;
  margin-bottom: 2rem;
`;

const HeaderContent = styled.div`
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(4px);
  border-radius: 1rem;
  padding: 1rem 2rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
`;

const HeaderInner = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
`;

const NavButton = styled.button`
  color: #6b7280;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 0.375rem;
  transition: color 0.2s ease;

  &:hover {
    color: #374151;
  }

  &:disabled {
    color: #d1d5db;
    cursor: not-allowed;
  }
`;

const NavIcon = styled.span`
  font-size: 1.2rem;
  display: inline-block;
`;

const HeaderInfo = styled.div`
  text-align: center;
  display: flex;
  align-items: center;
  gap: 2rem;
`;

const TitleText = styled.div`
  font-size: 32px;
  color: #374151;
`;

const DateDisplay = styled.div`
  font-size: 40px;
  font-weight: 500;
  color: #1f2937;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 0.5rem;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: rgba(0, 0, 0, 0.05);
  }
`;

const CountText = styled.div`
  font-size: 32px;
  color: #374151;
`;

const ListWrapper = styled.div`
  width: 100%;
  max-width: 64rem;
`;

const ListContent = styled.div`
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(4px);
  border-radius: 1rem;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
`;

const FilterSection = styled.div`
  margin-bottom: 1.5rem;
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  align-items: center;
`;

const FilterButton = styled.button`
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  border: none;
  background-color: ${props => props.active ? '#8b5cf6' : 'rgba(139, 92, 246, 0.1)'};
  color: ${props => props.active ? 'white' : '#8b5cf6'};
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;

  &:hover {
    background-color: ${props => props.active ? '#7c3aed' : 'rgba(139, 92, 246, 0.2)'};
  }
`;

const SearchInput = styled.input`
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  border: 1px solid #d1d5db;
  font-size: 14px;
  width: 200px;

  &:focus {
    outline: none;
    border-color: #8b5cf6;
  }
`;

const ListContainer = styled.div`
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  max-height: 60vh;
  overflow-y: auto;
`;

const DiaryItem = styled.div`
  background: linear-gradient(to right, #fce7f3, #f3e8ff);
  background-color: rgba(244, 221, 244, 0.6);
  border-radius: 1rem;
  padding: 1rem 1.5rem;
  cursor: pointer;
  transition: all 0.2s ease;
  opacity: ${props => props.isFiltered ? 0.5 : 1};

  &:hover {
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    transform: translateY(-1px);
  }
`;

const DiaryItemInner = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
`;

const DiaryDate = styled.div`
  color: #1f2937;
  font-weight: 500;
  font-size: 32px;
`;

const DiaryMood = styled.div`
  color: #374151;
  font-size: 32px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
`;

const HeaderPadding = styled.div`
  padding: 60px;
`;

const NoEntriesMessage = styled.div`
  text-align: center;
  color: #6b7280;
  font-size: 18px;
  padding: 2rem;
`;

const MonthSelector = styled.div`
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  padding: 1rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  z-index: 10;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.5rem;
  margin-top: 0.5rem;
`;

const MonthButton = styled.button`
  padding: 0.5rem;
  border: none;
  border-radius: 0.25rem;
  background-color: ${props => props.selected ? '#8b5cf6' : 'transparent'};
  color: ${props => props.selected ? 'white' : '#374151'};
  cursor: pointer;
  font-size: 14px;

  &:hover {
    background-color: ${props => props.selected ? '#7c3aed' : '#f3f4f6'};
  }
`;

const DiaryListPage = () => {
  const [currentDate, setCurrentDate] = useState({
    year: new Date().getFullYear(),
    month: new Date().getMonth() + 1
  });
  const [selectedMoodFilter, setSelectedMoodFilter] = useState('전체');
  const [searchTerm, setSearchTerm] = useState('');
  const [showMonthSelector, setShowMonthSelector] = useState(false);

  // 전체 일기 데이터 (실제로는 API에서 가져올 데이터)
  const generateSampleDiaryEntries = () => {
    const today = new Date();
    const year = today.getFullYear();
    const currentMonth = today.getMonth() + 1;

    const moods = ['기쁨', '슬픔', '분노', '두려움', '혐오감', '놀람', '신뢰감/사랑', '기타 복합 감정/혼합감정'];

    const entries = [];

    // 현재 월의 몇 개 일기
    for (let day = 1; day <= Math.min(today.getDate(), 15); day++) {
      if (Math.random() > 0.6) { // 60% 확률로 일기 생성
        entries.push({
          date: `${year}.${String(currentMonth).padStart(2, '0')}.${String(day).padStart(2, '0')}`,
          mood: moods[Math.floor(Math.random() * moods.length)]
        });
      }
    }

    // 이전 월의 몇 개 일기
    const prevMonth = currentMonth === 1 ? 12 : currentMonth - 1;
    const prevYear = currentMonth === 1 ? year - 1 : year;
    for (let day = 20; day <= 30; day++) {
      if (Math.random() > 0.7) { // 30% 확률로 일기 생성
        entries.push({
          date: `${prevYear}.${String(prevMonth).padStart(2, '0')}.${String(day).padStart(2, '0')}`,
          mood: moods[Math.floor(Math.random() * moods.length)]
        });
      }
    }

    return entries.sort((a, b) => new Date(b.date.replace(/\./g, '-')) - new Date(a.date.replace(/\./g, '-')));
  };

  const allDiaryEntries = generateSampleDiaryEntries();

  // 현재 월에 해당하는 일기들만 필터링
  const currentMonthEntries = useMemo(() => {
    return allDiaryEntries.filter(entry => {
      const entryDate = new Date(entry.date.replace(/\./g, '-'));
      return entryDate.getFullYear() === currentDate.year && 
             entryDate.getMonth() + 1 === currentDate.month;
    });
  }, [allDiaryEntries, currentDate]);

  // 필터링된 일기들
  const filteredEntries = useMemo(() => {
    let filtered = currentMonthEntries;

    // 감정 필터
    if (selectedMoodFilter !== '전체') {
      filtered = filtered.filter(entry => entry.mood === selectedMoodFilter);
    }

    // 검색어 필터
    if (searchTerm) {
      filtered = filtered.filter(entry => 
        entry.date.includes(searchTerm) || entry.mood.includes(searchTerm)
      );
    }

    return filtered;
  }, [currentMonthEntries, selectedMoodFilter, searchTerm]);

  // 8가지 기본 감정 목록
  const basicEmotions = ['전체', '기쁨', '슬픔', '분노', '두려움', '혐오감', '놀람', '신뢰감/사랑', '기타 복합 감정/혼합감정'];

  // 고유한 감정 목록 추출 (기본 감정을 우선으로 정렬)
  const uniqueMoods = useMemo(() => {
    const moodsFromData = [...new Set(allDiaryEntries.map(entry => entry.mood))];
    const orderedMoods = ['전체'];
    
    // 기본 감정 순서대로 추가
    basicEmotions.slice(1).forEach(emotion => {
      if (moodsFromData.includes(emotion)) {
        orderedMoods.push(emotion);
      }
    });
    
    // 기본 감정에 없는 감정들 추가
    moodsFromData.forEach(mood => {
      if (!basicEmotions.includes(mood) && !orderedMoods.includes(mood)) {
        orderedMoods.push(mood);
      }
    });
    
    return orderedMoods;
  }, [allDiaryEntries]);

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

  const handleDateClick = () => {
    setShowMonthSelector(!showMonthSelector);
  };

  const handleMonthSelect = (month) => {
    setCurrentDate({ ...currentDate, month });
    setShowMonthSelector(false);
  };

  const handleDiaryClick = (entry) => {
    // 일기 상세보기로 이동 (실제 구현시 navigate 사용)
    console.log('일기 클릭:', entry);
    // window.location.href = `/diary/${entry.date}`;
  };

  // 이전/다음 월에 일기가 있는지 확인
  const hasPrevMonth = useMemo(() => {
    const prevDate = currentDate.month === 1 
      ? { year: currentDate.year - 1, month: 12 }
      : { ...currentDate, month: currentDate.month - 1 };
    
    return allDiaryEntries.some(entry => {
      const entryDate = new Date(entry.date.replace(/\./g, '-'));
      return entryDate.getFullYear() === prevDate.year && 
             entryDate.getMonth() + 1 === prevDate.month;
    });
  }, [allDiaryEntries, currentDate]);

  const hasNextMonth = useMemo(() => {
    const nextDate = currentDate.month === 12 
      ? { year: currentDate.year + 1, month: 1 }
      : { ...currentDate, month: currentDate.month + 1 };
    
    return allDiaryEntries.some(entry => {
      const entryDate = new Date(entry.date.replace(/\./g, '-'));
      return entryDate.getFullYear() === nextDate.year && 
             entryDate.getMonth() + 1 === nextDate.month;
    });
  }, [allDiaryEntries, currentDate]);

  const totalEntries = currentMonthEntries.length;

  return (
    <>
    <Header />
    <HeaderPadding>
      <Container>
        <HeaderWrapper>
          <HeaderContent>
            <HeaderInner>
              <NavButton onClick={handlePrevMonth} disabled={!hasPrevMonth}>
                <NavIcon>◀</NavIcon>
              </NavButton>

              <HeaderInfo>
                <TitleText>내가 쓴 일기들</TitleText>
                <div style={{ position: 'relative' }}>
                  <DateDisplay onClick={handleDateClick}>
                    {currentDate.year}.{String(currentDate.month).padStart(2, '0')}
                  </DateDisplay>
                  {showMonthSelector && (
                    <MonthSelector>
                      {Array.from({ length: 12 }, (_, i) => i + 1).map(month => (
                        <MonthButton
                          key={month}
                          selected={month === currentDate.month}
                          onClick={() => handleMonthSelect(month)}
                        >
                          {month}월
                        </MonthButton>
                      ))}
                    </MonthSelector>
                  )}
                </div>
                <CountText>총 {totalEntries}개의 일기</CountText>
              </HeaderInfo>

              <NavButton onClick={handleNextMonth} disabled={!hasNextMonth}>
                <NavIcon>▶</NavIcon>
              </NavButton>
            </HeaderInner>
          </HeaderContent>
        </HeaderWrapper>

        <ListWrapper>
          <ListContent>
            <FilterSection>
              <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                {uniqueMoods.map(mood => (
                  <FilterButton
                    key={mood}
                    active={selectedMoodFilter === mood}
                    onClick={() => setSelectedMoodFilter(mood)}
                  >
                    {mood}
                  </FilterButton>
                ))}
              </div>
              <SearchInput
                type="text"
                placeholder="날짜 또는 감정으로 검색..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </FilterSection>

            <ListContainer>
              {filteredEntries.length > 0 ? (
                filteredEntries.map((entry, index) => (
                  <DiaryItem key={index} onClick={() => handleDiaryClick(entry)}>
                    <DiaryItemInner>
                      <DiaryDate>{entry.date}</DiaryDate>
                      <DiaryMood>{entry.mood}</DiaryMood>
                    </DiaryItemInner>
                  </DiaryItem>
                ))
              ) : (
                <NoEntriesMessage>
                  {currentMonthEntries.length === 0 
                    ? `${currentDate.year}년 ${currentDate.month}월에는 작성된 일기가 없습니다.`
                    : '검색 조건에 맞는 일기가 없습니다.'
                  }
                </NoEntriesMessage>
              )}
            </ListContainer>
          </ListContent>
        </ListWrapper>
      </Container>
    </HeaderPadding>
    <Footer />
    </>
  );
};

export default DiaryListPage;