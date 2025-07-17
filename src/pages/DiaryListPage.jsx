import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';

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

const ListContainer = styled.div`
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
`;

const DiaryItem = styled.div`
  background: linear-gradient(to right, #fce7f3, #f3e8ff);
  background-color: rgba(244, 221, 244, 0.6);
  border-radius: 1rem;
  padding: 1rem 1.5rem;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
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
`

const DiaryListPage = () => {
  const navigate = useNavigate();
  const [currentDate, setCurrentDate] = useState({ year: 2025, month: 6 });

  const diaryEntries = [
    { date: '2025.06.30', mood: '좋음' },
    { date: '2025.06.29', mood: '좋음' },
    { date: '2025.06.28', mood: '행복' },
    { date: '2025.06.20', mood: '슬픔' },
    { date: '2025.06.19', mood: '기쁨' },
    { date: '2025.06.16', mood: '좋음' },
    { date: '2025.06.11', mood: '슬픔' },
    { date: '2025.06.07', mood: '좋음' },
    { date: '2025.06.04', mood: '슬픔' },
    { date: '2025.06.01', mood: '슬픔' },
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
    <HeaderPadding>
    <Container>
      <HeaderWrapper>
        <HeaderContent>
          <HeaderInner>
            <NavButton onClick={handlePrevMonth}>
              <NavIcon>◀</NavIcon>
            </NavButton>

            <HeaderInfo>
              <TitleText>내가 쓴 일기들</TitleText>
              <DateDisplay>
                {currentDate.year}.{String(currentDate.month).padStart(2, '0')}
              </DateDisplay>
              <CountText>총 {totalEntries}개의 일기</CountText>
            </HeaderInfo>

            <NavButton onClick={handleNextMonth}>
              <NavIcon>▶</NavIcon>
            </NavButton>
          </HeaderInner>
        </HeaderContent>
      </HeaderWrapper>

      <ListWrapper>
        <ListContent>
          <ListContainer>
            {diaryEntries.map((entry, index) => (
              <DiaryItem key={index}>
                <DiaryItemInner>
                  <DiaryDate>{entry.date}</DiaryDate>
                  <DiaryMood>{entry.mood}</DiaryMood>
                </DiaryItemInner>
              </DiaryItem>
            ))}
          </ListContainer>
        </ListContent>
      </ListWrapper>
    </Container>
    </HeaderPadding>
  );
};

export default DiaryListPage;
