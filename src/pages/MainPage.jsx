import React, { useState, useEffect, useCallback, useMemo } from 'react';
import styled from 'styled-components';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';

// 스타일 컴포넌트 정의
const Wrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 85vh;
  padding-top: 80px;
  padding-bottom: 40px;
  box-sizing: border-box;
  overflow: hidden;
  

  @media (max-width: 768px) {
    padding-top: 80px;
    padding-bottom: 35px;
  }

  @media (max-width: 480px) {
    padding-top: 75px;
    padding-bottom: 30px;
  }

  @media (max-width: 320px) {
    padding-top: 70px;
    padding-bottom: 28px;
  }
`;

const CalendarContainer = styled.div`
  position: relative;
  width: 95%;
  max-width: 1400px;
  flex: 1;
  max-height: 100%;
  background-color: white;
  border-radius: 30px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  padding: 20px;
  box-sizing: border-box;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  margin: 0 auto;

  @media (max-width: 768px) {
    padding: 15px;
  }

  @media (max-width: 480px) {
    padding: 12px;
  }

  @media (max-width: 320px) {
    padding: 10px;
  }
`;

const MonthHeader = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 1.5rem;
  flex-shrink: 0;
`;

const MonthTitle = styled.h2`
  font-size: 2.2rem;
  font-weight: bold;
  color: #222;
  margin: 0;
  cursor: pointer;
  user-select: none;
`;

const WeekHeader = styled.div`
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  font-weight: 600;
  color: #666;
  margin-bottom: 0.5rem;
  flex-shrink: 0;
`;

const DayName = styled.div`
  padding: 0.75rem 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: ${({ index }) => (index === 0 ? '#e57373' : index === 6 ? '#64b5f6' : '#444')};
`;

const DaysGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  flex: 1;
  gap: 1px;
  overflow: hidden;

  @media (max-width: 768px) {
    gap: 1px;
  }
`;

const DayCell = styled.div`
  border: 1px solid #eee;
  padding: 3px;
  font-size: 11px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  overflow: hidden;
  min-height: 0;

  @media (max-width: 768px) {
    padding: 2px;
    font-size: 10px;
  }

  @media (max-width: 480px) {
    padding: 2px;
    font-size: 9px;
  }
`;

const DayNumber = styled.div`
  font-weight: bold;
  font-size: 1.5rem;
  color: ${({ isToday }) => (isToday ? '#e91e63' : '#333')};
  margin-bottom: 2px;
  flex-shrink: 0;

  @media (max-width: 768px) {
    font-size: 0.8rem;
    margin-bottom: 1px;
  }

  @media (max-width: 480px) {
    font-size: 0.7rem;
    margin-bottom: 1px;
  }
`;

const EventTag = styled.div`
  font-size: 18px;
  padding: 1px 2px;
  border-radius: 3px;
  margin-bottom: 0px;
  background-color: ${({ bg }) => bg || '#FF92D3'};
  color: ${({ text }) => text || 'black'};
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
  flex-shrink: 0;
  line-height: 0.85;

  @media (max-width: 768px) {
    font-size: 8px;
    padding: 1px 2px;
  }

  @media (max-width: 480px) {
    font-size: 7px;
    padding: 1px 2px;
  }
`;

// 로그인 오버레이 스타일 - 캘린더 컨테이너 내부에만 적용
const LoginOverlay = styled.div`
  font-family: '온글잎 의연체', sans-serif;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.8);
  z-index: 10;
  backdrop-filter: blur(2px);
  border-radius: 30px;
  padding-bottom: 8%;

  @media (max-width: 768px) {
    backdrop-filter: blur(1.5px);
    padding-bottom: 6%;
  }

  @media (max-width: 480px) {
    backdrop-filter: blur(1px);
    padding-bottom: 4%;
  }
`;

const LoginPromptTitle = styled.h1`
  font-size: 3rem;
  color: #333333;
  margin-bottom: 0.5rem;
  font-weight: bold;
  text-align: center;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

  @media (max-width: 768px) {
    font-size: 2.5rem;
  }

  @media (max-width: 480px) {
    font-size: 2rem;
  }
`;

const LoginPromptSubtitle = styled.p`
  font-size: 1.2rem;
  color: #666666;
  margin-bottom: 2rem;
  text-align: center;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);

  @media (max-width: 768px) {
    font-size: 1.1rem;
    margin-bottom: 1.5rem;
  }

  @media (max-width: 480px) {
    font-size: 1rem;
    margin-bottom: 1.2rem;
  }
`;

const LoginButton = styled.button`
  padding: 0.6rem 2rem;
  font-size: 1.1rem;
  background: linear-gradient(135deg, #ff4081);
  color: white;
  border: none;
  border-radius: 15px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(255, 64, 129, 0.3);
  width: 280px;
  
  &:hover {
    background: linear-gradient(135deg, #e91e63);
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(255, 64, 129, 0.5);
  }

  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
    transform: none;
  }

  @media (max-width: 768px) {
    padding: 0.5rem 1.5rem;
    font-size: 1rem;
    width: 240px;
  }

  @media (max-width: 480px) {
    padding: 0.5rem 1.2rem;
    font-size: 0.9rem;
    width: 200px;
  }
`;

const MainPage = () => {
  const navigate = useNavigate();
  const [currentDate, setCurrentDate] = useState(new Date());
  const [events, setEvents] = useState([]);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [showModal, setShowModal] = useState(false);


  const colors = useMemo(() => [
    { bg: '#f8bbd0', text: '#880e4f' },
    { bg: '#ce93d8', text: '#4a148c' },
    { bg: '#b2dfdb', text: '#004d40' },
    { bg: '#ffcc80', text: '#e65100' },
    { bg: '#90caf9', text: '#0d47a1' },
    { bg: '#d1c4e9', text: '#311b92' },
  ], []);

  const getRandomColor = useCallback((i) => colors[i % colors.length], [colors]);

  // 로그인 페이지로 이동하는 함수
  const handleLoginClick = useCallback(() => {
    navigate('/LoginPageOauth');
  }, [navigate]);

  // 이벤트 클릭 핸들러
  const handleEventClick = useCallback((event) => {
    setSelectedEvent(event);
    setShowModal(true);
  }, []);

  // 모달 닫기 핸들러
  const handleCloseModal = useCallback(() => {
    setShowModal(false);
    setSelectedEvent(null);
  }, []);

  // 날짜 포맷 함수
  const formatEventDate = useCallback((event) => {
    if (!event) return { startDate: '', endDate: '', isAllDay: false };

    const isAllDay = !!event.start.date;
    
    if (isAllDay) {
      const startDate = new Date(event.start.date);
      const endDate = event.end?.date ? new Date(event.end.date) : startDate;
      
      // 종일 이벤트의 경우 end는 다음날을 가리키므로 1일 빼기
      endDate.setDate(endDate.getDate() - 1);
      
      const formatDate = (date) => {
        return `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일`;
      };
      
      return {
        startDate: formatDate(startDate),
        endDate: formatDate(endDate),
        isAllDay: true
      };
    } else {
      const startDateTime = new Date(event.start.dateTime);
      const endDateTime = event.end?.dateTime ? new Date(event.end.dateTime) : startDateTime;
      
      const formatDateTime = (date) => {
        const year = date.getFullYear();
        const month = date.getMonth() + 1;
        const day = date.getDate();
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        return `${year}년 ${month}월 ${day}일 ${hours}:${minutes}`;
      };
      
      return {
        startDate: formatDateTime(startDateTime),
        endDate: formatDateTime(endDateTime),
        isAllDay: false
      };
    }
  }, []);

  // 샘플 이벤트 로드 함수
  const loadSampleEvents = useCallback(() => {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');

    const sampleEvents = [
      { id: '1', summary: '감정체크', start: { date: `${year}-${month}-01` } },
      { id: '2', summary: 'JAVA, SPRING', start: { date: `${year}-${month}-02` }, end: { date: `${year}-${month}-07` } },
      { id: '3', summary: '오송역시, 약속', start: { date: `${year}-${month}-02` } },
    ];
    setEvents(sampleEvents.map((e, i) => ({ ...e, color: getRandomColor(i) })));
  }, [getRandomColor]);

  // 백엔드 캘린더 이벤트 로드 함수
  const loadCalendarEvents = useCallback(async () => {
    if (!isAuthenticated) return;

    setIsLoading(true);
    try {
      const timeMin = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1).toISOString();
      const timeMax = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0).toISOString();

      const response = await api.get(`/api/calendar?timeMin=${timeMin}&timeMax=${timeMax}`);

      const calendarEvents = response.data || [];
      const formatted = calendarEvents.map((e, i) => ({
        id: e.id,
        summary: e.summary,
        start: e.start,
        end: e.end,
        color: getRandomColor(i),
      }));
      setEvents(formatted);
      console.log('캘린더 이벤트 로드 완료:', formatted.length, '개');
    } catch (error) {
      console.error('캘린더 이벤트 불러오기 실패:', error);
      loadSampleEvents();
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated, currentDate, getRandomColor, loadSampleEvents]);

  useEffect(() => {
    if (isAuthenticated) {
      loadCalendarEvents();
    }
  }, [currentDate, isAuthenticated, loadCalendarEvents]);

  useEffect(() => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    setIsAuthenticated(true);  
  } else {
    setIsAuthenticated(false);
    loadSampleEvents();
  }
}, []);

  const dateUtils = useMemo(() => ({
    getDaysInMonth: (date) => new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate(),
    getFirstDayOfMonth: (date) => new Date(date.getFullYear(), date.getMonth(), 1).getDay(),
    formatMonth: (date) => `${date.getFullYear()}년 ${date.getMonth() + 1}월`,
  }), []);

  const getDayEvents = useCallback((day) => {
    const dateStr = `${currentDate.getFullYear()}-${String(currentDate.getMonth() + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    return events.filter((event) => {
      // 종일 이벤트는 start.date, 시간 지정 이벤트는 start.dateTime 사용
      const start = event.start.date || event.start.dateTime?.split('T')[0];
      const end = event.end?.date || event.end?.dateTime?.split('T')[0] || start;
      
      // 종일 이벤트의 경우 end는 다음날을 가리키므로 조정
      if (event.start.date && event.end?.date) {
        return dateStr >= start && dateStr < end;
      }
      
      // 시간 지정 이벤트의 경우 같은 날짜만 비교
      return dateStr === start;
    });
  }, [events, currentDate]);

  const goToPreviousMonth = useCallback(() =>
    setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1)), [currentDate]);
  
  const goToNextMonth = useCallback(() =>
    setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1)), [currentDate]);

  const handleMonthClick = useCallback((e) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const width = rect.width;
    
    if (clickX < width / 2) {
      goToPreviousMonth();
    } else {
      goToNextMonth();
    }
  }, [goToPreviousMonth, goToNextMonth]);

  const calendarDays = useMemo(() => {
    const daysInMonth = dateUtils.getDaysInMonth(currentDate);
    const firstDay = dateUtils.getFirstDayOfMonth(currentDate);
    const today = new Date();
    const days = [];

    for (let i = 0; i < firstDay; i++) {
      days.push(<DayCell key={`empty-${i}`} />);
    }

    for (let day = 1; day <= daysInMonth; day++) {
      const isToday =
        today.getFullYear() === currentDate.getFullYear() &&
        today.getMonth() === currentDate.getMonth() &&
        today.getDate() === day;

      const dayEvents = getDayEvents(day);
      days.push(
        <DayCell key={day}>
          <DayNumber isToday={isToday}>{day}</DayNumber>
          {dayEvents.map((e) => (
            <EventTag 
              key={e.id} 
              bg={e.color?.bg} 
              text={e.color?.text}
              onClick={(event) => {
                event.stopPropagation();
                handleEventClick(e);
              }}
              style={{ cursor: 'pointer' }}
            >
              {e.summary}
            </EventTag>
          ))}
        </DayCell>
      );
    }

    return days;
  }, [currentDate, dateUtils, getDayEvents]);

  const dayNames = ['일', '월', '화', '수', '목', '금', '토'];

  const eventDetails = selectedEvent ? formatEventDate(selectedEvent) : null;

  const eventDetails = selectedEvent ? formatEventDate(selectedEvent) : null;
  
  const LYDImageClick = () => {
    navigate('/DiaryWritingPage');
  }
  return (
    <Wrapper>
      {/* <Header /> */}
            {/* // =================== 왼쪽 하단 캐릭터 */}
            <LYDCWrapper onClick={LYDImageClick}>
      <SpeechBubble>냐옹~ 오늘도 좋은 하루야!</SpeechBubble>
      <LYDCImage src={LYDC} alt="고양이" />
    </LYDCWrapper>
    {/* // =================== 왼쪽 하단 캐릭터 */}
      
      <CalendarContainer>
        <MonthHeader>
          <MonthTitle onClick={handleMonthClick}>
            &lt; {dateUtils.formatMonth(currentDate)} &gt;
          </MonthTitle>
        </MonthHeader>

        {isLoading && (
          <div style={{ textAlign: 'center', margin: '1rem 0' }}>
            캘린더 데이터를 불러오는 중...
          </div>
        )}

        <WeekHeader>
          {dayNames.map((d, i) => (
            <DayName key={d} index={i}>
              {d}
            </DayName>
          ))}
        </WeekHeader>
        
        <DaysGrid>{calendarDays}</DaysGrid>

        {/* 로그인하지 않았을 때 캘린더 컨테이너에만 오버레이 표시 */}
        {!isAuthenticated && (
          <LoginOverlay>
            <LoginPromptTitle>EmoJournal 시작하기</LoginPromptTitle>
            <LoginPromptSubtitle>로그인 후 이용해 주세요</LoginPromptSubtitle>
            <LoginButton onClick={handleLoginClick} disabled={isLoading}>
              로그인
            </LoginButton>
          </LoginOverlay>
        )}
      </CalendarContainer>
      
      {/* 이벤트 상세 모달 */}
      {showModal && selectedEvent && eventDetails && (
        <ModalOverlay onClick={handleCloseModal}>
          <ModalContainer onClick={(e) => e.stopPropagation()}>
            <ModalHeader>
              <ModalTitle>제목추가</ModalTitle>
              <CloseButton onClick={handleCloseModal}>✕</CloseButton>
            </ModalHeader>
            
            <ModalContent>
              <DateRangeSection>
                <DateText>{eventDetails.startDate}</DateText>
                <DateText>→</DateText>
                <DateText>{eventDetails.endDate}</DateText>
              </DateRangeSection>
              
              <AllDayCheckbox>
                <CheckboxIcon>{eventDetails.isAllDay ? '✓' : ''}</CheckboxIcon>
                <CheckboxLabel>종일</CheckboxLabel>
              </AllDayCheckbox>
            </ModalContent>
            
            <ModalFooter>
              <SaveButton onClick={handleCloseModal}>저장</SaveButton>
            </ModalFooter>
          </ModalContainer>
        </ModalOverlay>
      )}
      
      <Footer />
    </Wrapper>
  );
};

export default MainPage;