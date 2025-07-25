import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';

// Styled Components
const LoginContainer = styled.div`
  min-height: 78vh;
  display: flex;
  align-items: center;
  justify-content: center;
`;

const LoginCard = styled.div`
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  padding: 2rem;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  width: 100%;
  max-width: 400px;
  margin: 1rem;
`;

const Title = styled.h1`
  font-size: 2.5rem;
  font-weight: bold;
  color: #374151;
  text-align: center;
  margin-bottom: 0.5rem;
`;

const Subtitle = styled.p`
  color: #6b7280;
  font-size: 1.4rem;
  text-align: center;
  margin-bottom: 2rem;
  font-family: '온글잎 의연체', sans-serif;
`;

const GoogleButton = styled.button`
  width: 100%;
  background: white;
  color: #374151;
  font-weight: 600;
  padding: 0.75rem 1rem;
  border-radius: 50px;
  border: 1px solid #d1d5db;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  
  &:hover {
    background: #f9fafb;
    transform: scale(1.02);
  }
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
    transform: none;
  }
`;

const ErrorMessage = styled.div`
  background: #fee2e2;
  color: #dc2626;
  padding: 0.75rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  font-size: 0.875rem;
  text-align: center;
  font-family: '온글잎 의연체', sans-serif;
`;

const LoginPage = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  // OAuth 2.0용 Google API 초기화
  useEffect(() => {
    // Google API Platform 라이브러리 로드 (OAuth 2.0용)
    const script = document.createElement('script');
    script.src = 'https://apis.google.com/js/api.js';
    script.async = true;
    script.defer = true;
    document.body.appendChild(script);

    script.onload = () => {
      console.log('Google API Platform loaded');
      
      // Google API 초기화
      window.gapi.load('auth2', () => {
        // OAuth 2.0 초기화 설정
        window.gapi.auth2.init({
          client_id: process.env.REACT_APP_GOOGLE_CLIENT_ID, // 구글 클라우드 콘솔에서 발급받은 클라이언트 ID
          scope: [
            'openid',                                    // 기본 사용자 식별
            'email',                                     // 이메일 주소 접근
            'profile',                                   // 프로필 정보 접근
            'https://www.googleapis.com/auth/calendar'   // Google Calendar 읽기/쓰기 권한
          ].join(' '),
          // 추가 스코프가 필요한 경우:
          // 'https://www.googleapis.com/auth/calendar.readonly' - 캘린더 읽기 전용
          // 'https://www.googleapis.com/auth/calendar.events' - 이벤트 관리만
        }).then(() => {
          console.log('Google OAuth 2.0 초기화 완료');
        }).catch((error) => {
          console.error('Google OAuth 2.0 초기화 실패:', error);
          setError('Google 로그인 서비스 초기화에 실패했습니다.');
        });
      });
    };

    script.onerror = () => {
      console.error('Google API 스크립트 로드 실패');
      setError('Google API를 불러올 수 없습니다.');
    };

    return () => {
      if (document.body.contains(script)) {
        document.body.removeChild(script);
      }
    };
  }, []);

  // OAuth 2.0 로그인 성공 후 토큰 처리
  const handleGoogleResponse = async (googleUser) => {
    console.log('Google OAuth 사용자 객체:', googleUser);
    setIsLoading(true);
    setError('');
    
    try {
      // OAuth 2.0에서 받은 토큰 정보들
      const authResponse = googleUser.getAuthResponse();
      const accessToken = authResponse.access_token;    // Google API 호출용 액세스 토큰
      const idToken = authResponse.id_token;            // 사용자 식별용 ID 토큰
      const expiresIn = authResponse.expires_in;        // 토큰 만료 시간 (초)
      
      // 사용자 기본 프로필 정보
      const profile = googleUser.getBasicProfile();
      const userInfo = {
        id: profile.getId(),
        name: profile.getName(),
        email: profile.getEmail(),
        imageUrl: profile.getImageUrl()
      };
      
      console.log('액세스 토큰:', accessToken);
      console.log('ID 토큰:', idToken);
      console.log('사용자 정보:', userInfo);
      console.log('토큰 만료 시간:', expiresIn, '초');
      
      // 로컬 스토리지에 토큰 및 사용자 정보 저장
      localStorage.setItem('googleAccessToken', accessToken);  // Google Calendar API 호출용
      localStorage.setItem('googleIdToken', idToken);           // 백엔드 인증용
      localStorage.setItem('tokenExpiresAt', Date.now() + (expiresIn * 1000)); // 만료 시간
      localStorage.setItem('userInfo', JSON.stringify(userInfo));
      
      // 백엔드 API 호출 (선택사항 - 서버에서 사용자 정보 저장하려는 경우)
      try {
        const apiResponse = await fetch('/api/auth/google-oauth', {
          method: 'POST',
          headers: { 
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}` // 액세스 토큰을 헤더에 포함
          },
          body: JSON.stringify({ 
            accessToken: accessToken,
            idToken: idToken,
            userInfo: userInfo,
            expiresIn: expiresIn
          })
        });

        if (apiResponse.ok) {
          const data = await apiResponse.json();
          console.log('백엔드 응답:', data);
          
          // 백엔드에서 추가 토큰을 제공하는 경우 저장
          if (data.serverToken) {
            localStorage.setItem('serverToken', data.serverToken);
          }
        } else {
          console.warn('백엔드 API 호출 실패, 하지만 로그인은 계속 진행');
        }
      } catch (apiError) {
        console.warn('백엔드 API 연결 실패:', apiError);
        // 백엔드 연결 실패해도 로그인은 계속 진행
      }
      
      // 메인 페이지로 이동
      navigate('/MainPage');
      
    } catch (error) {
      console.error('OAuth 2.0 로그인 처리 오류:', error);
      setError('로그인 처리 중 오류가 발생했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  // OAuth 2.0 로그인 버튼 클릭 핸들러
  const handleGoogleLogin = () => {
    setIsLoading(true);
    setError('');
    
    try {
      // Google OAuth 2.0 인스턴스 가져오기
      const authInstance = window.gapi.auth2.getAuthInstance();
      
      if (!authInstance) {
        throw new Error('Google OAuth 2.0이 초기화되지 않았습니다.');
      }
      
      // OAuth 2.0 로그인 팝업 실행
      authInstance.signIn({
        prompt: 'select_account' // 계정 선택 화면 강제 표시 (선택사항)
      }).then((googleUser) => {
        // 로그인 성공
        handleGoogleResponse(googleUser);
      }).catch((error) => {
        console.error('Google OAuth 2.0 로그인 오류:', error);
        
        if (error.error === 'popup_closed_by_user') {
          setError('로그인이 취소되었습니다.');
        } else if (error.error === 'access_denied') {
          setError('Google 로그인 권한이 거부되었습니다.');
        } else {
          setError('Google 로그인 중 오류가 발생했습니다.');
        }
        
        setIsLoading(false);
      });
      
    } catch (error) {
      console.error('OAuth 2.0 로그인 초기화 오류:', error);
      setError('Google 로그인 서비스를 사용할 수 없습니다.');
      setIsLoading(false);
    }
  };

  // Google Calendar API 호출 예시 함수 (참고용)
  const callGoogleCalendarAPI = async () => {
    const accessToken = localStorage.getItem('googleAccessToken');
    const expiresAt = localStorage.getItem('tokenExpiresAt');
    
    // 토큰 만료 확인
    if (!accessToken || Date.now() > parseInt(expiresAt)) {
      console.error('액세스 토큰이 없거나 만료되었습니다. 다시 로그인해주세요.');
      return;
    }
    
    try {
      // Google Calendar API 호출 예시
      const response = await fetch('https://www.googleapis.com/calendar/v3/calendars/primary/events', {
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        const calendarData = await response.json();
        console.log('캘린더 데이터:', calendarData);
        return calendarData;
      } else {
        console.error('Calendar API 호출 실패:', response.status);
      }
    } catch (error) {
      console.error('Calendar API 호출 오류:', error);
    }
  };

  return (
    <>
      <LoginContainer>
        <LoginCard>
          {/* 제목 */}
          <Title>LOGIN</Title>
          <Subtitle>Google 계정으로 로그인해주세요</Subtitle>

          {/* 에러 메시지 */}
          {error && <ErrorMessage>{error}</ErrorMessage>}

          {/* Google OAuth 2.0 로그인 버튼 */}
          <GoogleButton
            onClick={handleGoogleLogin}
            disabled={isLoading}
          >
            <svg width="20" height="20" viewBox="0 0 24 24">
              <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
              <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
              <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
              <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
            </svg>
            {isLoading ? '로그인 중...' : 'Google로 로그인'}
          </GoogleButton>
        </LoginCard>
      </LoginContainer>
    </>
  );
};

export default LoginPage;