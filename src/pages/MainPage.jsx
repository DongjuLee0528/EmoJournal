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

// 로그인 오버레이 스타일
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
  padding-bottom: 10%;

  @media (max-width: 768px) {
    padding-bottom: 8%;
  }

  @media (max-width: 480px) {
    padding-bottom: 5%;
  }
`;

const LoginPromptTitle = styled.h1`
  font-size: 3rem;
  color: #333;
  margin-bottom: 0.5rem;
  font-weight: bold;
  text-align: center;

  @media (max-width: 768px) {
    font-size: 2.5rem;
  }

  @media (max-width: 480px) {
    font-size: 2rem;
  }
`;

const LoginPromptSubtitle = styled.p`
  font-size: 1.2rem;
  color: #666;
  margin-bottom: 1rem;
  text-align: center;

  @media (max-width: 768px) {
    font-size: 1.1rem;
    margin-bottom: 0.8rem;
  }

  @media (max-width: 480px) {
    font-size: 1rem;
    margin-bottom: 0.8rem;
  }
`;

const LoginButton = styled.button`
  padding: 0.4rem 3rem;
  font-size: 1.2rem;
  background: #ff80ab;
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 600;
  
  &:hover {
    background: #ff4081;
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(255, 64, 129, 0.4);
  }

  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
    transform: none;
  }

  @media (max-width: 768px) {
    padding: 0.8rem 2.5rem;
    font-size: 1.1rem;
  }

  @media (max-width: 480px) {
    padding: 0.7rem 2rem;
    font-size: 1rem;
  }
`;

const MainPage = () => {
  const navigate = useNavigate();
  const [currentDate, setCurrentDate] = useState(new Date(2025, 5, 1));
  const [events, setEvents] = useState([]);
  const [isGoogleApiLoaded, setIsGoogleApiLoaded] = useState(false);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const config = useMemo(() => ({
    GOOGLE_API_KEY: process.env.REACT_APP_GOOGLE_API_KEY,
    CLIENT_ID: process.env.REACT_APP_GOOGLE_CLIENT_ID,
    CALENDAR_ID: 'primary',
    DISCOVERY_DOC: 'https://www.googleapis.com/discovery/v1/apis/calendar/v3/rest',
    SCOPES: 'https://www.googleapis.com/auth/calendar.readonly'
  }), []);

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

  // 샘플 이벤트 로드 함수
  const loadSampleEvents = useCallback(() => {
    const sampleEvents = [
      { id: '1', summary: '감정체크', start: { date: '2025-06-01' } },
      { id: '2', summary: 'JAVA, SPRING', start: { date: '2025-06-02' }, end: { date: '2025-06-07' } },
      { id: '3', summary: '오송역시, 약속', start: { date: '2025-06-02' } },
    ];
    setEvents(sampleEvents.map((e, i) => ({ ...e, color: getRandomColor(i) })));
  }, [getRandomColor]);

  // 구글 캘린더 이벤트 로드 함수
  const loadGoogleCalendarEvents = useCallback(async () => {
    if (!isGoogleApiLoaded || !isAuthenticated) return;

    setIsLoading(true);
    try {
      const response = await window.gapi.client.calendar.events.list({
        calendarId: config.CALENDAR_ID,
        timeMin: new Date(currentDate.getFullYear(), currentDate.getMonth(), 1).toISOString(),
        timeMax: new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0).toISOString(),
        showDeleted: false,
        singleEvents: true,
        orderBy: 'startTime',
      });

      const googleEvents = response.result.items || [];
      const formatted = googleEvents.map((e, i) => ({
        id: e.id,
        summary: e.summary,
        start: e.start,
        end: e.end,
        color: getRandomColor(i),
      }));
      setEvents(formatted);
      console.log('구글 캘린더 이벤트 로드 완료:', formatted.length, '개');
    } catch (error) {
      console.error('이벤트 불러오기 실패:', error);
      loadSampleEvents();
    } finally {
      setIsLoading(false);
    }
  }, [isGoogleApiLoaded, isAuthenticated, currentDate, config.CALENDAR_ID, getRandomColor, loadSampleEvents]);

  useEffect(() => {
    if (isAuthenticated && isGoogleApiLoaded) {
      loadGoogleCalendarEvents();
    }
  }, [currentDate, isAuthenticated, isGoogleApiLoaded, loadGoogleCalendarEvents]);

  useEffect(() => {
    const initializeGapi = async () => {
      if (!config.GOOGLE_API_KEY || !config.CLIENT_ID) {
        console.warn('API 키 미설정. 샘플 데이터 사용.');
        loadSampleEvents();
        return;
      }

      if (window.gapi) {
        window.gapi.load('client:auth2', async () => {
          try {
            await window.gapi.client.init({
              apiKey: config.GOOGLE_API_KEY,
              clientId: config.CLIENT_ID,
              discoveryDocs: [config.DISCOVERY_DOC],
              scope: config.SCOPES,
            });

            setIsGoogleApiLoaded(true);
            const authInstance = window.gapi.auth2.getAuthInstance();
            
            authInstance.isSignedIn.listen((isSignedIn) => {
              setIsAuthenticated(isSignedIn);
              if (isSignedIn) {
                loadGoogleCalendarEvents();
              } else {
                loadSampleEvents();
              }
            });

            if (authInstance.isSignedIn.get()) {
              setIsAuthenticated(true);
              loadGoogleCalendarEvents();
            } else {
              loadSampleEvents();
            }
          } catch (error) {
            console.error('GAPI 초기화 실패:', error);
            loadSampleEvents();
          }
        });
      } else {
        loadSampleEvents();
      }
    };

    if (!window.gapi && config.GOOGLE_API_KEY && config.CLIENT_ID) {
      const script = document.createElement('script');
      script.src = 'https://apis.google.com/js/api.js';
      script.onload = initializeGapi;
      document.body.appendChild(script);
    } else {
      initializeGapi();
    }
  }, [config, loadGoogleCalendarEvents, loadSampleEvents]);

  const dateUtils = useMemo(() => ({
    getDaysInMonth: (date) => new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate(),
    getFirstDayOfMonth: (date) => new Date(date.getFullYear(), date.getMonth(), 1).getDay(),
    formatMonth: (date) => `${date.getFullYear()}년 ${date.getMonth() + 1}월`,
  }), []);

  const getDayEvents = useCallback((day) => {
    const dateStr = `${currentDate.getFullYear()}-${String(currentDate.getMonth() + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    return events.filter((event) => {
      const start = event.start.date || event.start.dateTime?.split('T')[0];
      const end = event.end?.date || start;
      return dateStr >= start && dateStr < end;
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
            <EventTag key={e.id} bg={e.color?.bg} text={e.color?.text}>
              {e.summary}
            </EventTag>
          ))}
        </DayCell>
      );
    }

    return days;
  }, [currentDate, dateUtils, getDayEvents]);

  const dayNames = ['일', '월', '화', '수', '목', '금', '토'];

  return (
    <Wrapper>
      <Header />
      
      <CalendarContainer style={{ position: 'relative' }}>
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

        {/* 로그인하지 않았을 때 오버레이 표시 */}
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
      
      <Footer />
    </Wrapper>
  );
};

export default MainPage;