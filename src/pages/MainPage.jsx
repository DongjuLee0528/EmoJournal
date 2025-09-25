import React, { useState, useEffect, useCallback, useMemo } from 'react';
import styled from 'styled-components';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';
import api from '../api/axiosInstance';

// 기존 스타일 컴포넌트들
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
  cursor: pointer;
  transition: opacity 0.2s ease;

  &:hover {
    opacity: 0.8;
  }

  @media (max-width: 768px) {
    font-size: 8px;
    padding: 1px 2px;
  }

  @media (max-width: 480px) {
    font-size: 7px;
    padding: 1px 2px;
  }
`;

// 이벤트 수정 팝업 스타일
const PopupOverlay = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.3);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
`;

const PopupContainer = styled.div`
  position: relative;
  background: linear-gradient(135deg, #f8bbd9 0%, #e1bee7 100%);
  border-radius: 20px;
  padding: 30px;
  min-width: 400px;
  max-width: 500px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  font-family: '온글잎 의연체', sans-serif;
  animation: slideIn 0.3s ease-out;

  @keyframes slideIn {
    from {
      transform: translateX(100px);
      opacity: 0;
    }
    to {
      transform: translateX(0);
      opacity: 1;
    }
  }

  @media (max-width: 768px) {
    min-width: 300px;
    max-width: 90vw;
    padding: 25px;
  }

  @media (max-width: 480px) {
    min-width: 280px;
    padding: 20px;
  }
`;

const PopupHeader = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
`;

const PopupTitle = styled.h2`
  font-size: 1.8rem;
  font-weight: bold;
  color: #333;
  margin: 0;

  @media (max-width: 768px) {
    font-size: 1.5rem;
  }
`;

const CloseButton = styled.button`
  background: none;
  border: none;
  font-size: 2rem;
  font-weight: bold;
  color: #333;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: rgba(255, 255, 255, 0.3);
  }
`;

const PopupContent = styled.div`
  margin-bottom: 25px;
`;

const EventTitle = styled.input`
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 10px;
  font-size: 1.1rem;
  margin-bottom: 15px;
  background-color: rgba(255, 255, 255, 0.8);
  box-sizing: border-box;

  &:focus {
    outline: none;
    background-color: rgba(255, 255, 255, 1);
    box-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
  }
`;

const DateRange = styled.div`
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
  font-size: 1rem;
  color: #333;

  @media (max-width: 480px) {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
`;

const DateInput = styled.input`
  padding: 8px;
  border: none;
  border-radius: 8px;
  background-color: rgba(255, 255, 255, 0.8);
  font-size: 1rem;

  &:focus {
    outline: none;
    background-color: rgba(255, 255, 255, 1);
  }
`;

const CheckboxContainer = styled.div`
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 25px;
`;

const Checkbox = styled.input`
  width: 18px;
  height: 18px;
  accent-color: #e91e63;
`;

const CheckboxLabel = styled.label`
  font-size: 1rem;
  color: #333;
  cursor: pointer;
`;

const ButtonContainer = styled.div`
  display: flex;
  justify-content: flex-end;
  gap: 15px;
`;

const ActionButton = styled.button`
  padding: 10px 20px;
  border: none;
  border-radius: 15px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 80px;

  &:hover {
    transform: translateY(-2px);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none;
  }
`;

const DeleteButton = styled(ActionButton)`
  background: linear-gradient(135deg, #a8c8ec, #7fb3d3);
  color: #1565c0;

  &:hover:not(:disabled) {
    background: linear-gradient(135deg, #90b4e8, #5a9bd4);
  }
`;

const SaveButton = styled(ActionButton)`
  background: linear-gradient(135deg, #81c784, #66bb6a);
  color: white;

  &:hover:not(:disabled) {
    background: linear-gradient(135deg, #6fbf73, #4caf50);
  }
`;

const ModifyButton = styled(ActionButton)`
  background: linear-gradient(135deg, #a8c8ec, #7fb3d3);
  color: #1565c0;

  &:hover:not(:disabled) {
    background: linear-gradient(135deg, #90b4e8, #5a9bd4);
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

// 디버그 패널 스타일 (개발용)
const DebugPanel = styled.div`
  position: fixed;
  top: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 10px;
  border-radius: 5px;
  font-size: 12px;
  max-width: 300px;
  z-index: 1000;
  display: ${({ show }) => show ? 'block' : 'none'};
`;

const MainPage = () => {
  const navigate = useNavigate();
  const [currentDate, setCurrentDate] = useState(new Date());
  const [events, setEvents] = useState([]);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  
  // 팝업 관련 상태
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [showPopup, setShowPopup] = useState(false);
  const [editedTitle, setEditedTitle] = useState('');
  const [editedStartDate, setEditedStartDate] = useState('');
  const [editedEndDate, setEditedEndDate] = useState('');
  const [isAllDay, setIsAllDay] = useState(false);
  const [isEditing, setIsEditing] = useState(false);

  // 디버그용 상태
  const [debugInfo, setDebugInfo] = useState('');
  const [showDebug, setShowDebug] = useState(false);

  const colors = useMemo(() => [
    { bg: '#f8bbd0', text: '#880e4f' },
    { bg: '#ce93d8', text: '#4a148c' },
    { bg: '#b2dfdb', text: '#004d40' },
    { bg: '#ffcc80', text: '#e65100' },
    { bg: '#90caf9', text: '#0d47a1' },
    { bg: '#d1c4e9', text: '#311b92' },
  ], []);

  const getRandomColor = useCallback((i) => colors[i % colors.length], [colors]);

  // 날짜 포맷팅 함수 개선
  const formatDateForComparison = useCallback((dateStr) => {
    if (!dateStr) return null;
    
    // ISO 형식의 날짜에서 날짜 부분만 추출
    if (dateStr.includes('T')) {
      return dateStr.split('T')[0];
    }
    return dateStr;
  }, []);

  // 이벤트 클릭 핸들러
  const handleEventClick = useCallback((event) => {
    setSelectedEvent(event);
    setEditedTitle(event.summary || '');
    setEditedStartDate(formatDateForComparison(event.start.date || event.start.dateTime) || '');
    setEditedEndDate(formatDateForComparison(event.end?.date || event.end?.dateTime || event.start.date || event.start.dateTime) || '');
    setIsAllDay(!!event.start.date);
    setIsEditing(false);
    setShowPopup(true);
  }, [formatDateForComparison]);

  // 팝업 닫기
  const closePopup = useCallback(() => {
    setShowPopup(false);
    setSelectedEvent(null);
    setIsEditing(false);
  }, []);

  // 이벤트 수정 시작
  const startEditing = useCallback(() => {
    setIsEditing(true);
  }, []);

  // 이벤트 저장
  const saveEvent = useCallback(async () => {
    if (!selectedEvent) return;

    try {
      setIsLoading(true);
      
      const updatedEvent = {
        ...selectedEvent,
        summary: editedTitle,
        start: isAllDay 
          ? { date: editedStartDate }
          : { dateTime: `${editedStartDate}T00:00:00` },
        end: isAllDay
          ? { date: editedEndDate }
          : { dateTime: `${editedEndDate}T23:59:59` }
      };

      // API 호출 (실제 구현에서는 적절한 엔드포인트 사용)
      await api.put(`/api/calendar/${selectedEvent.id}`, updatedEvent);
      
      // 로컬 상태 업데이트
      setEvents(prevEvents => 
        prevEvents.map(event => 
          event.id === selectedEvent.id ? updatedEvent : event
        )
      );

      setIsEditing(false);
      closePopup();
      console.log('이벤트 수정 완료');
    } catch (error) {
      console.error('이벤트 수정 실패:', error);
      alert('이벤트 수정에 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  }, [selectedEvent, editedTitle, editedStartDate, editedEndDate, isAllDay, closePopup]);

  // 이벤트 삭제
  const deleteEvent = useCallback(async () => {
    if (!selectedEvent) return;

    if (!window.confirm('이 일정을 삭제하시겠습니까?')) return;

    try {
      setIsLoading(true);
      
      // API 호출 (실제 구현에서는 적절한 엔드포인트 사용)
      await api.delete(`/api/calendar/${selectedEvent.id}`);
      
      // 로컬 상태 업데이트
      setEvents(prevEvents => 
        prevEvents.filter(event => event.id !== selectedEvent.id)
      );

      closePopup();
      console.log('이벤트 삭제 완료');
    } catch (error) {
      console.error('이벤트 삭제 실패:', error);
      alert('이벤트 삭제에 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  }, [selectedEvent, closePopup]);

  // 로그인 페이지로 이동
  const handleLoginClick = useCallback(() => {
    navigate('/LoginPageOauth');
  }, [navigate]);

  // 백엔드 캘린더 이벤트 로드
  const loadCalendarEvents = useCallback(async () => {
    if (!isAuthenticated) return;

    setIsLoading(true);
    try {
      const timeMin = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1).toISOString();
      const timeMax = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0).toISOString();

      const response = await api.get(`/api/calendar?timeMin=${timeMin}&timeMax=${timeMax}`);

      const calendarEvents = response.data || [];
      
      // 디버그 정보 업데이트
      setDebugInfo(`API로부터 받은 이벤트: ${calendarEvents.length}개`);
      
      const formatted = calendarEvents.map((e, i) => ({
        id: e.id,
        summary: e.summary,
        start: e.start,
        end: e.end,
        color: getRandomColor(i),
      }));
      
      setEvents(formatted);
      console.log('캘린더 이벤트 로드 완료:', formatted.length, '개');
      console.log('이벤트 상세:', formatted);
      
    } catch (error) {
      console.error('캘린더 이벤트 불러오기 실패:', error);
      setEvents([]);
      setDebugInfo('API 호출 실패');
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated, currentDate, getRandomColor]);

  useEffect(() => {
    if (isAuthenticated) {
      loadCalendarEvents();
    }
  }, [currentDate, isAuthenticated, loadCalendarEvents]);

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      setIsAuthenticated(true);
      loadCalendarEvents();
    } else {
      setIsAuthenticated(false);
      setEvents([]);
    }
  }, [loadCalendarEvents]);

  const dateUtils = useMemo(() => ({
    getDaysInMonth: (date) => new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate(),
    getFirstDayOfMonth: (date) => new Date(date.getFullYear(), date.getMonth(), 1).getDay(),
    formatMonth: (date) => `${date.getFullYear()}년 ${date.getMonth() + 1}월`,
  }), []);

  // 개선된 getDayEvents 함수
  const getDayEvents = useCallback((day) => {
    const dateStr = `${currentDate.getFullYear()}-${String(currentDate.getMonth() + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    
    const dayEvents = events.filter((event) => {
      const startDate = formatDateForComparison(event.start.date || event.start.dateTime);
      const endDate = formatDateForComparison(event.end?.date || event.end?.dateTime || event.start.date || event.start.dateTime);
      
      if (!startDate) return false;
      
      // 종일 이벤트의 경우, end 날짜는 실제 종료일 다음날로 설정되므로 조정
      let actualEndDate = endDate;
      if (event.start.date && event.end?.date) {
        // 종일 이벤트인 경우 end 날짜에서 하루를 빼줌
        const endDateObj = new Date(endDate);
        endDateObj.setDate(endDateObj.getDate() - 1);
        actualEndDate = endDateObj.toISOString().split('T')[0];
      }
      
      const isInRange = dateStr >= startDate && dateStr <= actualEndDate;
      
      // 디버그 로그 (개발 시에만 사용)
      if (day === 1) { // 매월 1일에만 로그 출력
        console.log(`Event: "${event.summary}"`);
        console.log(`Start: ${startDate}, End: ${endDate}, Actual End: ${actualEndDate}`);
        console.log(`Date: ${dateStr}, In Range: ${isInRange}`);
        console.log('---');
      }
      
      return isInRange;
    });
    
    return dayEvents;
  }, [events, currentDate, formatDateForComparison]);

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
    let totalEventsShown = 0;

    for (let i = 0; i < firstDay; i++) {
      days.push(<DayCell key={`empty-${i}`} />);
    }

    for (let day = 1; day <= daysInMonth; day++) {
      const isToday =
        today.getFullYear() === currentDate.getFullYear() &&
        today.getMonth() === currentDate.getMonth() &&
        today.getDate() === day;

      const dayEvents = getDayEvents(day);
      totalEventsShown += dayEvents.length;
      
      days.push(
        <DayCell key={day}>
          <DayNumber isToday={isToday}>{day}</DayNumber>
          {dayEvents.map((e) => (
            <EventTag 
              key={e.id} 
              bg={e.color?.bg} 
              text={e.color?.text}
              onClick={() => handleEventClick(e)}
              title={e.summary} // 툴팁 추가
            >
              {e.summary}
            </EventTag>
          ))}
        </DayCell>
      );
    }

    // 디버그 정보 업데이트
    setDebugInfo(prev => `${prev} | 화면에 표시된 이벤트: ${totalEventsShown}개`);

    return days;
  }, [currentDate, dateUtils, getDayEvents, handleEventClick]);

  const dayNames = ['일', '월', '화', '수', '목', '금', '토'];

  // 디버그 패널 토글 (개발용)
  const toggleDebug = useCallback(() => {
    setShowDebug(prev => !prev);
  }, []);

  return (
    <Wrapper>
      <Header />
      
      {/* 개발용 디버그 버튼 */}
      <button 
        onClick={toggleDebug}
        style={{
          position: 'fixed',
          top: '10px',
          left: '10px',
          zIndex: 1001,
          padding: '5px 10px',
          fontSize: '12px',
          background: '#333',
          color: 'white',
          border: 'none',
          borderRadius: '3px'
        }}
      >
        Debug
      </button>
      
      <DebugPanel show={showDebug}>
        <div>현재 월: {dateUtils.formatMonth(currentDate)}</div>
        <div>전체 이벤트 수: {events.length}</div>
        <div>{debugInfo}</div>
        <div>인증 상태: {isAuthenticated ? '로그인됨' : '로그인 필요'}</div>
      </DebugPanel>
      
      <CalendarContainer>
        <MonthHeader>
          <MonthTitle onClick={handleMonthClick}>
            &lt; {dateUtils.formatMonth(currentDate)} &gt;
          </MonthTitle>
        </MonthHeader>

        {isLoading && !showPopup && (
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
      
      {/* 이벤트 수정/삭제 팝업 */}
      {showPopup && selectedEvent && (
        <PopupOverlay onClick={closePopup}>
          <PopupContainer onClick={e => e.stopPropagation()}>
            <PopupHeader>
              <PopupTitle>제목수정</PopupTitle>
              <CloseButton onClick={closePopup}>×</CloseButton>
            </PopupHeader>

            <PopupContent>
              <EventTitle
                value={editedTitle}
                onChange={(e) => setEditedTitle(e.target.value)}
                disabled={!isEditing}
                placeholder="일정 제목"
              />

              <DateRange>
                <DateInput
                  type="date"
                  value={editedStartDate}
                  onChange={(e) => setEditedStartDate(e.target.value)}
                  disabled={!isEditing}
                />
                <span>—</span>
                <DateInput
                  type="date"
                  value={editedEndDate}
                  onChange={(e) => setEditedEndDate(e.target.value)}
                  disabled={!isEditing}
                />
              </DateRange>

              <CheckboxContainer>
                <Checkbox
                  type="checkbox"
                  id="allDay"
                  checked={isAllDay}
                  onChange={(e) => setIsAllDay(e.target.checked)}
                  disabled={!isEditing}
                />
                <CheckboxLabel htmlFor="allDay">종일</CheckboxLabel>
              </CheckboxContainer>
            </PopupContent>

            <ButtonContainer>
              <DeleteButton onClick={deleteEvent} disabled={isLoading}>
                삭제
              </DeleteButton>
              {!isEditing ? (
                <ModifyButton onClick={startEditing} disabled={isLoading}>
                  수정
                </ModifyButton>
              ) : (
                <SaveButton onClick={saveEvent} disabled={isLoading}>
                  저장
                </SaveButton>
              )}
            </ButtonContainer>
          </PopupContainer>
        </PopupOverlay>
      )}
      
      <Footer />
    </Wrapper>
  );
};

export default MainPage;