import React, { useState, useEffect, useCallback, useMemo } from 'react';
import styled from 'styled-components';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import Footer from '../components/Footer';
import api from '../api/axiosInstance';

import LYDC from '../image/LYDC.png';

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

const CalendarGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-auto-rows: minmax(80px, 1fr);
  gap: 1px;
  background-color: #eee;
  border: 1px solid #eee;
  flex: 1;
  overflow: hidden;

  @media (max-width: 768px) {
    grid-auto-rows: minmax(60px, 1fr);
  }

  @media (max-width: 480px) {
    grid-auto-rows: minmax(50px, 1fr);
  }
`;

const DayCell = styled.div`
  background-color: white;
  padding: 8px;
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  @media (max-width: 768px) {
    padding: 5px;
  }

  @media (max-width: 480px) {
    padding: 3px;
  }
`;

const DayNumber = styled.div`
  font-weight: bold;
  font-size: 1.5rem;
  color: ${({ isToday }) => (isToday ? '#e91e63' : '#333')};
  margin-bottom: 5px;
  position: relative;
  z-index: 1;

  @media (max-width: 768px) {
    font-size: 1rem;
    margin-bottom: 3px;
  }

  @media (max-width: 480px) {
    font-size: 0.8rem;
    margin-bottom: 2px;
  }
`;

const EventsContainer = styled.div`
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-auto-rows: 26px;
  gap: 1px;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  padding: 45px 21px 21px 21px;

  @media (max-width: 768px) {
    grid-auto-rows: 21px;
    padding: 38px 16px 16px 16px;
  }

  @media (max-width: 480px) {
    grid-auto-rows: 17px;
    padding: 33px 13px 13px 13px;
  }

  @media (max-width: 320px) {
    grid-auto-rows: 15px;
    padding: 31px 11px 11px 11px;
  }
`;

const EventBar = styled.div`
  grid-column: ${({ startCol, span }) => `${startCol} / span ${span}`};
  grid-row: ${({ row }) => row};
  background-color: ${({ bg }) => bg || '#FF92D3'};
  color: ${({ text }) => text || 'black'};
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  margin: 1px 0;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  position: relative;
  z-index: 2;
  box-shadow: 0 1px 3px rgba(0,0,0,0.12);
  pointer-events: auto;
  min-height: 24px;
  display: flex;
  align-items: center;
  
  &:hover {
    opacity: 0.9;
    box-shadow: 0 2px 5px rgba(0,0,0,0.2);
    z-index: 3;
  }

  @media (max-width: 768px) {
    font-size: 11px;
    padding: 3px 6px;
    min-height: 20px;
  }

  @media (max-width: 480px) {
    font-size: 9px;
    padding: 2px 4px;
    min-height: 16px;
  }

  @media (max-width: 320px) {
    font-size: 8px;
    padding: 2px 3px;
    min-height: 14px;
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

// 이벤트 상세 모달 스타일
const ModalOverlay = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
`;

const ModalContainer = styled.div`
  background: linear-gradient(135deg, #e1bee7 0%, #f3e5f5 100%);
  border-radius: 20px;
  padding: 25px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  position: relative;
`;

const ModalHeader = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
`;

const ModalTitle = styled.h2`
  font-size: 1.3rem;
  color: #4a148c;
  margin: 0;
  font-weight: bold;
`;

const CloseButton = styled.button`
  background: none;
  border: none;
  font-size: 1.8rem;
  color: #4a148c;
  cursor: pointer;
  width: 35px;
  height: 35px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s;
  
  &:hover {
    background-color: rgba(74, 20, 140, 0.1);
  }
`;

const ModalContent = styled.div`
  margin-bottom: 20px;
`;

const DateRangeSection = styled.div`
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  padding: 12px;
  background-color: white;
  border-radius: 10px;
`;

const DateText = styled.span`
  font-size: 0.95rem;
  color: #333;
  font-weight: 500;
`;

const AllDayCheckbox = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 15px;
`;

const CheckboxIcon = styled.div`
  width: 20px;
  height: 20px;
  background-color: #64b5f6;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.8rem;
`;

const CheckboxLabel = styled.span`
  font-size: 0.95rem;
  color: #333;
`;

const ModalFooter = styled.div`
  display: flex;
  justify-content: flex-end;
`;

const SaveButton = styled.button`
  background-color: #b39ddb;
  color: white;
  border: none;
  padding: 10px 30px;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
  
  &:hover {
    background-color: #9575cd;
  }
`;

// 왼쪽 하단 캐릭터
const LYDCWrapper = styled.div`
  position: fixed;
  bottom: 20px;
  left: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 2000;
`;

const SpeechBubble = styled.div`
  position: relative;
  background: #fff;
  border-radius: 12px;
  padding: 12px 16px;
  color: #333;
  font-size: 14px;
  font-family: "Noto Sans KR", sans-serif;
  box-shadow: 0 2px 6px rgba(0,0,0,0.2);

  &::after {
    content: '';
    position: absolute;
    bottom: -15px;
    left: 50%;
    border-width: 15px 10px 0 10px;
    border-style: solid;
    border-color: #fff transparent transparent transparent;
  }
`;

const LYDCImage = styled.img`
  width: 250px;
  height: auto;
  cursor: pointer;
  transform: scaleX(-1);
`;

const MainPage = () => {
  const navigate = useNavigate();
  const [currentDate, setCurrentDate] = useState(new Date());
  const [events, setEvents] = useState([]);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editTitle, setEditTitle] = useState('');
  const [editAllDay, setEditAllDay] = useState(false);
  const [editStart, setEditStart] = useState('');
  const [editEnd, setEditEnd] = useState('');

  const colors = useMemo(() => [
    { bg: '#f8bbd0', text: '#880e4f' },
    { bg: '#ce93d8', text: '#4a148c' },
    { bg: '#b2dfdb', text: '#004d40' },
    { bg: '#ffcc80', text: '#e65100' },
    { bg: '#90caf9', text: '#0d47a1' },
    { bg: '#d1c4e9', text: '#311b92' },
  ], []);

  const getRandomColor = useCallback((i) => colors[i % colors.length], [colors]);

  const handleLoginClick = useCallback(() => {
    navigate('/LoginPageOauth');
  }, [navigate]);

  const handleEventClick = useCallback((event) => {
    setSelectedEvent(event);
    setShowModal(true);
    // 초기 편집 값 설정
    const isAllDay = !!event.start?.date;
    setEditTitle(event.summary || '');
    setEditAllDay(isAllDay);
    if (isAllDay) {
      // all-day: date 형식
      const start = event.start?.date || '';
      const endRaw = event.end?.date || start;
      // 구글 캘린더 all-day end는 다음날을 가리키므로 저장 시 -1일 표시를 위해 모달에는 실제 기간의 마지막 날을 보여줌
      const endDate = new Date(endRaw);
      endDate.setDate(endDate.getDate() - 1);
      const end = `${endDate.getFullYear()}-${String(endDate.getMonth() + 1).padStart(2, '0')}-${String(endDate.getDate()).padStart(2, '0')}`;
      setEditStart(start);
      setEditEnd(end);
    } else {
      // timed: datetime-local 형식
      const toLocal = (iso) => {
        if (!iso) return '';
        const d = new Date(iso);
        const yyyy = d.getFullYear();
        const mm = String(d.getMonth() + 1).padStart(2, '0');
        const dd = String(d.getDate()).padStart(2, '0');
        const hh = String(d.getHours()).padStart(2, '0');
        const min = String(d.getMinutes()).padStart(2, '0');
        return `${yyyy}-${mm}-${dd}T${hh}:${min}`;
      };
      setEditStart(toLocal(event.start?.dateTime));
      setEditEnd(toLocal(event.end?.dateTime));
    }
  }, []);

  const handleCloseModal = useCallback(() => {
    setShowModal(false);
    setSelectedEvent(null);
    setEditTitle('');
    setEditAllDay(false);
    setEditStart('');
    setEditEnd('');
  }, []);

  const formatEventDate = useCallback((event) => {
    if (!event) return { startDate: '', endDate: '', isAllDay: false };

    const isAllDay = !!event.start.date;
    
    if (isAllDay) {
      const startDate = new Date(event.start.date);
      const endDate = event.end?.date ? new Date(event.end.date) : startDate;
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
  }, [loadSampleEvents]);

  const dateUtils = useMemo(() => ({
    getDaysInMonth: (date) => new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate(),
    getFirstDayOfMonth: (date) => new Date(date.getFullYear(), date.getMonth(), 1).getDay(),
    formatMonth: (date) => `${date.getFullYear()}년 ${date.getMonth() + 1}월`,
  }), []);

  const applyLocalUpdate = useCallback((updatedEvent) => {
    setEvents((prev) => prev.map((e) => (e.id === updatedEvent.id ? { ...e, ...updatedEvent } : e)));
  }, []);

  const applyLocalDelete = useCallback((eventId) => {
    setEvents((prev) => prev.filter((e) => e.id !== eventId));
  }, []);

  const handleSaveEvent = useCallback(async () => {
    if (!selectedEvent) return;

    try {
      const payload = { summary: editTitle };
      if (editAllDay) {
        // all-day: end는 다음날 날짜로 전송
        const startDate = new Date(editStart);
        const endDate = new Date(editEnd);
        endDate.setDate(endDate.getDate() + 1);
        const fmt = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
        payload.start = { date: fmt(startDate) };
        payload.end = { date: fmt(endDate) };
      } else {
        // timed: ISO로 전송
        const toISO = (s) => (s ? new Date(s).toISOString() : undefined);
        payload.start = { dateTime: toISO(editStart) };
        payload.end = { dateTime: toISO(editEnd) };
      }

      if (isAuthenticated) {
        await api.put(`/api/calendar/${selectedEvent.id}`, payload);
      }

      const updated = {
        ...selectedEvent,
        summary: payload.summary,
        start: payload.start,
        end: payload.end,
      };
      applyLocalUpdate(updated);
      handleCloseModal();
    } catch (err) {
      console.error('이벤트 수정 실패:', err);
      alert('이벤트 수정에 실패했습니다.');
    }
  }, [selectedEvent, editTitle, editAllDay, editStart, editEnd, isAuthenticated, applyLocalUpdate, handleCloseModal]);

  const handleDeleteEvent = useCallback(async () => {
    if (!selectedEvent) return;
    const confirmed = window.confirm('이 일정을 삭제하시겠습니까?');
    if (!confirmed) return;
    try {
      if (isAuthenticated) {
        await api.delete(`/api/calendar/${selectedEvent.id}`);
      }
      applyLocalDelete(selectedEvent.id);
      handleCloseModal();
    } catch (err) {
      console.error('이벤트 삭제 실패:', err);
      alert('이벤트 삭제에 실패했습니다.');
    }
  }, [selectedEvent, isAuthenticated, applyLocalDelete, handleCloseModal]);

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

  // 연결된 이벤트 바로 처리
  const processedEvents = useMemo(() => {
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();
    const daysInMonth = dateUtils.getDaysInMonth(currentDate);
    const firstDay = dateUtils.getFirstDayOfMonth(currentDate);
    
    const eventSegments = [];
    
    events.forEach(event => {
      const startDate = event.start.date || event.start.dateTime?.split('T')[0];
      const endDate = event.end?.date || event.end?.dateTime?.split('T')[0] || startDate;
      
      const start = new Date(startDate);
      const end = new Date(endDate);
      
      // 종일 이벤트의 경우 end는 다음날을 가리키므로 1일 빼기
      if (event.start.date && event.end?.date) {
        end.setDate(end.getDate() - 1);
      }
      
      // 이번 달에 해당하는 일정만 처리
      if (start.getFullYear() === year && start.getMonth() === month) {
        const startDay = start.getDate();
        const endDay = Math.min(end.getDate(), daysInMonth);
        
        let currentDay = startDay;
        
        while (currentDay <= endDay) {
          const dayIndex = currentDay - 1 + firstDay;
          const startCol = (dayIndex % 7) + 1;
          const weekRow = Math.floor(dayIndex / 7);
          
          const daysLeftInWeek = 7 - startCol + 1;
          const daysLeftInEvent = endDay - currentDay + 1;
          const span = Math.min(daysLeftInWeek, daysLeftInEvent);
          
          eventSegments.push({
            ...event,
            startCol,
            span,
            row: weekRow + 2,
            segmentStart: currentDay,
            segmentEnd: currentDay + span - 1,
          });
          
          currentDay += span;
        }
      }
    });
    
    // 겹치는 이벤트 처리
    const rowMap = {};
    eventSegments.forEach(segment => {
      const weekKey = segment.row;
      if (!rowMap[weekKey]) {
        rowMap[weekKey] = [];
      }
      
      let availableRow = segment.row;
      let foundSlot = false;
      
      while (!foundSlot) {
        const overlaps = rowMap[weekKey].some(existingSegment => 
          existingSegment.row === availableRow &&
          !(segment.startCol > existingSegment.startCol + existingSegment.span - 1 ||
            segment.startCol + segment.span - 1 < existingSegment.startCol)
        );
        
        if (!overlaps) {
          foundSlot = true;
          segment.row = availableRow;
          rowMap[weekKey].push(segment);
        } else {
          availableRow++;
        }
      }
    });
    
    return eventSegments;
  }, [events, currentDate, dateUtils]);

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

      days.push(
        <DayCell key={day}>
          <DayNumber isToday={isToday}>{day}</DayNumber>
        </DayCell>
      );
    }

    return days;
  }, [currentDate, dateUtils]);

  const dayNames = ['일', '월', '화', '수', '목', '금', '토'];
  const eventDetails = selectedEvent ? formatEventDate(selectedEvent) : null;
  
  const LYDImageClick = () => {
    navigate('/DiaryWritingPage');
  }

  return (
    <Wrapper>
      <LYDCWrapper onClick={LYDImageClick}>
        <SpeechBubble>냐옹~ 오늘도 좋은 하루야!</SpeechBubble>
        <LYDCImage src={LYDC} alt="고양이" />
      </LYDCWrapper>
      
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
        
        <div style={{ position: 'relative', flex: 1, overflow: 'hidden' }}>
          <CalendarGrid>{calendarDays}</CalendarGrid>
          
          <EventsContainer>
            {processedEvents.map((event, idx) => (
              <EventBar
                key={`${event.id}-${idx}`}
                startCol={event.startCol}
                span={event.span}
                row={event.row}
                bg={event.color?.bg}
                text={event.color?.text}
                onClick={() => handleEventClick(event)}
              >
                {event.summary}
              </EventBar>
            ))}
          </EventsContainer>
        </div>

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
      
      {showModal && selectedEvent && eventDetails && (
        <ModalOverlay onClick={handleCloseModal}>
          <ModalContainer onClick={(e) => e.stopPropagation()}>
            <ModalHeader>
              <ModalTitle>제목추가</ModalTitle>
              <CloseButton onClick={handleCloseModal}>✕</CloseButton>
            </ModalHeader>
            
            <ModalContent>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginBottom: '12px' }}>
                <label style={{ fontSize: '0.95rem', color: '#4a148c', fontWeight: 600 }}>제목</label>
                <input
                  type="text"
                  value={editTitle}
                  onChange={(e) => setEditTitle(e.target.value)}
                  style={{ padding: '10px 12px', borderRadius: '8px', border: '1px solid #ddd' }}
                />
              </div>
              <DateRangeSection>
              {editAllDay ? (
                <>
                  <input
                    type="date"
                    value={editStart}
                    onChange={(e) => setEditStart(e.target.value)}
                    style={{ padding: '8px', borderRadius: '8px', border: '1px solid #ddd' }}
                  />
                  <DateText>→</DateText>
                  <input
                    type="date"
                    value={editEnd}
                    onChange={(e) => setEditEnd(e.target.value)}
                    style={{ padding: '8px', borderRadius: '8px', border: '1px solid #ddd' }}
                  />
                </>
              ) : (
                <>
                  <input
                    type="datetime-local"
                    value={editStart}
                    onChange={(e) => setEditStart(e.target.value)}
                    style={{ padding: '8px', borderRadius: '8px', border: '1px solid #ddd' }}
                  />
                  <DateText>→</DateText>
                  <input
                    type="datetime-local"
                    value={editEnd}
                    onChange={(e) => setEditEnd(e.target.value)}
                    style={{ padding: '8px', borderRadius: '8px', border: '1px solid #ddd' }}
                  />
                </>
              )}
              </DateRangeSection>
              
              <AllDayCheckbox>
              <div onClick={() => setEditAllDay(!editAllDay)} style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                <CheckboxIcon>{editAllDay ? '✓' : ''}</CheckboxIcon>
                <CheckboxLabel>종일</CheckboxLabel>
              </div>
              </AllDayCheckbox>
            </ModalContent>
            
            <ModalFooter>
              <button
                onClick={handleDeleteEvent}
                style={{
                  backgroundColor: '#ef9a9a',
                  color: 'white',
                  border: 'none',
                  padding: '10px 18px',
                  borderRadius: '10px',
                  fontSize: '1rem',
                  fontWeight: 600,
                  cursor: 'pointer',
                  marginRight: '10px'
                }}
              >
                삭제
              </button>
              <SaveButton onClick={handleSaveEvent}>저장</SaveButton>
            </ModalFooter>
          </ModalContainer>
        </ModalOverlay>
      )}
      
      <Footer />
    </Wrapper>
  );
};

export default MainPage;