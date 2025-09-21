import React, { useState, useMemo } from 'react';
import styled from 'styled-components';
import Header from '../components/Header';
import Footer from '../components/Footer';

const Container = styled.div`
  font-family: '온글잎 의연체', sans-serif;
  width: 97%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 2rem;
  max-width: 1920px;
`;

const HeaderWrapper = styled.div`
  width: 100%;
  max-width: 64rem;
  margin-bottom: 2rem;
`;

const HeaderContent = styled.div`
  position: relative;
  z-index: 50;
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
  background-color: ${props => (props.active ? '#8b5cf6' : 'rgba(139, 92, 246, 0.1)')};
  color: ${props => (props.active ? 'white' : '#8b5cf6')};
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;

  &:hover {
    background-color: ${props => (props.active ? '#7c3aed' : 'rgba(139, 92, 246, 0.2)')};
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
`;

const DiaryItem = styled.div`
  background: linear-gradient(to right, #fce7f3, #f3e8ff);
  border-radius: 1rem;
  padding: 1rem 1.5rem;
  cursor: pointer;
  transition: all 0.2s ease;
  opacity: ${props => (props.isFiltered ? 0.5 : 1)};

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
  padding-top: 60px;
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
  z-index: 10000;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.5rem;
  margin-top: 0.5rem;
`;

const MonthButton = styled.button`
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 0.5rem;
  border: none;
  border-radius: 0.25rem;
  background-color: ${props => (props.selected ? '#8b5cf6' : 'transparent')};
  color: ${props => (props.selected ? 'white' : '#374151')};
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
  white-space: nowrap;

  &:hover {
    background-color: ${props => (props.selected ? '#7c3aed' : '#f3f4f6')};
  }

  .num {
    font-weight: 600;
  }
  .unit {
    font-size: 12px;
    opacity: 0.9;
  }
`;

const DiaryListPage = () => {
  const [currentDate, setCurrentDate] = useState({ year: 2025, month: 6 });
  const [selectedMoodFilter, setSelectedMoodFilter] = useState('전체');
  const [searchTerm, setSearchTerm] = useState('');
  const [showMonthSelector, setShowMonthSelector] = useState(false);

  const allDiaryEntries = [
    { date: '2025.06.30', mood: '기쁨' },
    { date: '2025.06.29', mood: '신뢰감/사랑' },
    { date: '2025.06.28', mood: '기쁨' },
    { date: '2025.06.20', mood: '슬픔' },
    { date: '2025.06.19', mood: '놀람' },
    { date: '2025.06.16', mood: '기쁨' },
    { date: '2025.06.11', mood: '분노' },
    { date: '2025.06.07', mood: '두려움' },
    { date: '2025.06.04', mood: '슬픔' },
    { date: '2025.06.01', mood: '혐오감' },
    { date: '2025.05.30', mood: '기타 복합 감정/혼합감정' },
    { date: '2025.05.25', mood: '기쁨' },
    { date: '2025.05.20', mood: '신뢰감/사랑' },
    { date: '2025.07.05', mood: '놀람' },
    { date: '2025.07.10', mood: '기쁨' },
  ];

  const currentMonthEntries = useMemo(() => {
    return allDiaryEntries.filter(entry => {
      const entryDate = new Date(entry.date.replace(/\./g, '-'));
      return (
        entryDate.getFullYear() === currentDate.year &&
        entryDate.getMonth() + 1 === currentDate.month
      );
    });
  }, [allDiaryEntries, currentDate]);

  const filteredEntries = useMemo(() => {
    let filtered = currentMonthEntries;

    if (selectedMoodFilter !== '전체') {
      filtered = filtered.filter(entry => entry.mood === selectedMoodFilter);
    }

    if (searchTerm) {
      filtered = filtered.filter(
        entry => entry.date.includes(searchTerm) || entry.mood.includes(searchTerm),
      );
    }

    return filtered;
  }, [currentMonthEntries, selectedMoodFilter, searchTerm]);

  const basicEmotions = [
    '전체',
    '기쁨',
    '슬픔',
    '분노',
    '두려움',
    '혐오감',
    '놀람',
    '신뢰감/사랑',
    '기타 복합 감정/혼합감정',
  ];

  const uniqueMoods = useMemo(() => {
    const moodsFromData = [...new Set(allDiaryEntries.map(entry => entry.mood))];
    const orderedMoods = ['전체'];

    basicEmotions.slice(1).forEach(emotion => {
      if (moodsFromData.includes(emotion)) {
        orderedMoods.push(emotion);
      }
    });

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

  const handleMonthSelect = month => {
    setCurrentDate({ ...currentDate, month });
    setShowMonthSelector(false);
  };

  const handleDiaryClick = entry => {
    console.log('일기 클릭:', entry);
  };

  const totalEntries = currentMonthEntries.length;

  return (
    <>
      <Header />
      <HeaderPadding>
        <Container>
          <HeaderWrapper>
            <HeaderContent>
              <HeaderInner>
                {/* ◀ 버튼 */}
                <NavButton onClick={handlePrevMonth}>
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
                            <span className="num">{month}</span>
                            <span className="unit">월</span>
                          </MonthButton>
                        ))}
                      </MonthSelector>
                    )}
                  </div>
                  <CountText>총 {totalEntries}개의 일기</CountText>
                </HeaderInfo>

                {/* ▶ 버튼 */}
                <NavButton onClick={handleNextMonth}>
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
                  onChange={e => setSearchTerm(e.target.value)}
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
                      : '검색 조건에 맞는 일기가 없습니다.'}
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
