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
  const [currentDate, setCurrentDate] = useState(new Date(2025, 5, 1));
  const [events, setEvents] = useState([]);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  // API 기본 설정
  const apiConfig = useMemo(() => ({
    baseURL: process.env.REACT_APP_API_BASE_URL || '',
    timeout: 10000
  }), []);

  // API 호출 헬퍼 함수
  const apiCall = useCallback(async (endpoint, options = {}) => {
    const token = localStorage.getItem('access_token');
    const url = `${apiConfig.baseURL}${endpoint}`;
    
    const defaultOptions = {
      headers: {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` })
      },
      credentials: 'include',
      ...options
    };

    try {
      const response = await fetch(url, defaultOptions);
      
      if (response.status === 401) {
        localStorage.removeItem('access_token');
        setIsAuthenticated(false);
        throw new Error('Unauthorized');
      }

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error(`API 호출 실패 (${endpoint}):`, error);
      throw error;
    }
  }, [apiConfig.baseURL]);

  // 백엔드에서 제공한 Google OAuth 설정
  const config = useMemo(() => ({
    CLIENT_ID: '639506784430-mvf0oth3lt0jc4nab5dbjq18ki7nggsv.apps.googleusercontent.com',
    REDIRECT_URI: 'https://emojournal.djloghub.com/oauth/callback',
    SCOPES: [
      'https://www.googleapis.com/auth/calendar.readonly',
      'https://www.googleapis.com/auth/userinfo.email',
      'https://www.googleapis.com/auth/userinfo.profile'
    ].join(' ')
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

  // 백엔드 OAuth URL로 리다이렉트하는 함수
  const handleLoginClick = useCallback(() => {
    const authUrl = `https://accounts.google.com/o/oauth2/v2/auth?` +
      `client_id=${config.CLIENT_ID}&` +
      `redirect_uri=${encodeURIComponent(config.REDIRECT_URI)}&` +
      `response_type=code&` +
      `scope=${encodeURIComponent(config.SCOPES)}&` +
      `access_type=offline&` +
      `prompt=consent`;
    
    window.location.href = authUrl;
  }, [config]);

  // 샘플 이벤트 로드 함수
  const loadSampleEvents = useCallback(() => {
    const sampleEvents = [
      { id: '1', summary: '감정체크', start: { date: '2025-06-01' } },
      { id: '2', summary: 'JAVA, SPRING', start: { date: '2025-06-02' }, end: { date: '2025-06-07' } },
      { id: '3', summary: '오송역시, 약속', start: { date: '2025-06-02' } },
      { id: '4', summary: '팀 미팅', start: { date: '2025-06-05' } },
      { id: '5', summary: '프로젝트 발표', start: { date: '2025-06-10' } },
      { id: '6', summary: '개인 일정', start: { date: '2025-06-15' } },
    ];
    setEvents(sampleEvents.map((e, i) => ({ ...e, color: getRandomColor(i) })));
  }, [getRandomColor]);

  // 백엔드에서 캘린더 데이터를 가져오는 함수
  const loadCalendarEvents = useCallback(async () => {
    if (!isAuthenticated) {
      loadSampleEvents();
      return;
    }

    setIsLoading(true);
    try {
      const params = new URLSearchParams({
        year: currentDate.getFullYear().toString(),
        month: (currentDate.getMonth() + 1).toString()
      });

      const data = await apiCall(`/api/calendar/events?${params}`);
      
      const formatted = data.map((e, i) => ({
        id: e.id,
        summary: e.summary,
        start: e.start,
        end: e.end,
        color: getRandomColor(i),
      }));
      
      setEvents(formatted);
      console.log('백엔드에서 캘린더 이벤트 로드 완료:', formatted.length, '개');
    } catch (error) {
      console.error('캘린더 이벤트 불러오기 실패:', error);
      if (error.message !== 'Unauthorized') {
        loadSampleEvents();
      }
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated, currentDate, getRandomColor, loadSampleEvents, apiCall]);

  // OAuth 콜백 처리 및 인증 상태 확인
  useEffect(() => {
    const checkAuthStatus = async () => {
      try {
        // URL에서 authorization code 확인
        const urlParams = new URLSearchParams(window.location.search);
        const code = urlParams.get('code');
        const error = urlParams.get('error');

        if (error) {
          console.error('OAuth 에러:', error);
          return;
        }

        if (code) {
          // 백엔드에 authorization code 전송
          const data = await apiCall('/api/oauth/callback', {
            method: 'POST',
            body: JSON.stringify({ code })
          });
          
          localStorage.setItem('access_token', data.access_token);
          setIsAuthenticated(true);
          
          // URL에서 code 파라미터 제거
          const newUrl = window.location.pathname;
          window.history.replaceState({}, document.title, newUrl);
        } else {
          // 기존 토큰 확인
          const token = localStorage.getItem('access_token');
          if (token) {
            try {
              await apiCall('/api/auth/verify');
              setIsAuthenticated(true);
            } catch (error) {
              console.log('토큰 검증 실패:', error);
              localStorage.removeItem('access_token');
              setIsAuthenticated(false);
            }
          }
        }
      } catch (error) {
        console.error('인증 상태 확인 실패:', error);
        setIsAuthenticated(false);
      }
    };

    checkAuthStatus();
  }, [apiCall]);

  // 인증 상태나 현재 날짜가 변경될 때 이벤트 로드
  useEffect(() => {
    loadCalendarEvents();
  }, [currentDate, isAuthenticated, loadCalendarEvents]);

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
      
      <Footer />
    </Wrapper>
  );
};

export default MainPage;