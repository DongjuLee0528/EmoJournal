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

  // Google 로그인 초기화
  useEffect(() => {
    const script = document.createElement('script');
    script.src = 'https://accounts.google.com/gsi/client';
    script.async = true;
    script.defer = true;
    document.body.appendChild(script);

    script.onload = () => {
      console.log('Google Identity Services loaded');
      // 실제 Google 초기화
      if (window.google && process.env.REACT_APP_GOOGLE_CLIENT_ID) {
        window.google.accounts.id.initialize({
          client_id: process.env.REACT_APP_GOOGLE_CLIENT_ID,
          callback: handleGoogleResponse
        });
      }
    };

    return () => {
      if (document.body.contains(script)) {
        document.body.removeChild(script);
      }
    };
  }, []);

// 역할:
// 구글 로그인용 공식 JavaScript 라이브러리(Google Identity Services)를 외부에서 동적으로 불러오는 부분.
// 이걸 통해 구글 로그인 기능을 사용할 수 있어짐.
// 주의점:
// process.env.REACT_APP_GOOGLE_CLIENT_ID에 구글 클라우드 콘솔에서 발급받은 클라이언트 ID가 반드시 환경변수로 설정되어 있어야 함.
// window.google.accounts.id.initialize()를 호출해서 구글 로그인 초기 설정(클라이언트 ID + 콜백 함수) 하는 부분.
// 해야 할 일:
// 구글 클라우드 플랫폼에서 OAuth 클라이언트 ID 생성
// .env 파일 등에 REACT_APP_GOOGLE_CLIENT_ID=발급받은_클라이언트_ID 세팅


  // 백엔드 1: Google JWT 토큰 검증 및 사용자 인증
  const handleGoogleResponse = async (response) => {
    console.log('Google JWT:', response.credential);
    setIsLoading(true);
    setError('');
    
    try {
      // JWT 토큰을 디코드하여 사용자 정보 확인 (프론트엔드에서 디버깅용)
      const payload = JSON.parse(atob(response.credential.split('.')[1]));
      console.log('사용자 정보:', payload);
      
      // 백엔드 API 호출 - 실제 구현 시 이 부분을 백엔드 개발자와 연동
      const apiResponse = await fetch('/api/auth/google', {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          // 필요시 추가 헤더
        },
        body: JSON.stringify({ 
          token: response.credential,
          // 필요시 추가 데이터
        })
      });

      const data = await apiResponse.json();
      
      if (data.success) {
        // 백엔드에서 받은 사용자 토큰을 로컬 스토리지에 저장
        if (data.accessToken) {
          localStorage.setItem('accessToken', data.accessToken);
        }
        if (data.refreshToken) {
          localStorage.setItem('refreshToken', data.refreshToken);
        }
        
        // 사용자 정보도 필요시 저장
        if (data.user) {
          localStorage.setItem('user', JSON.stringify(data.user));
        }
        
        // 대시보드로 이동
        navigate('/MainPage');
      } else {
        setError(data.message || '로그인에 실패했습니다.');
      }
      
    } catch (error) {
      console.error('Google 로그인 오류:', error);
      setError('로그인 처리 중 오류가 발생했습니다. 네트워크를 확인해주세요.');
    } finally {
      setIsLoading(false);
    }
  };

//   역할:
// 구글에서 로그인 성공 후 구글에서 받은 JWT 토큰(response.credential)을 받아 처리하는 함수.
// JWT 토큰에서 사용자 정보(이메일, 이름 등)를 디코드해 볼 수 있음 (디버깅 목적).
// 중요: 백엔드 API로 토큰을 전송해서 구글 서버와 인증을 검증받아야 함(토큰 위변조 확인 및 사용자 등록/로그인 처리).
// 백엔드가 accessToken, refreshToken, user 정보를 주면 로컬스토리지에 저장.
// 로그인 성공 시 navigate('/MainPage')로 페이지 이동.
// 해야 할 일:
// 백엔드에서 /api/auth/google 경로를 만들고, 구글 토큰 검증 및 사용자 정보 처리 로직 구현
// 프론트엔드와 백엔드 API 연동


  const handleGoogleLogin = () => {
    setIsLoading(true);
    setError('');
    
    // 실제 Google 로그인 실행
    if (window.google && window.google.accounts) {
      window.google.accounts.id.prompt();
      // 프롬프트가 뜨면 로딩 해제
      setTimeout(() => setIsLoading(false), 1000);
    } else {
      console.error('Google Identity Services not loaded');
      setError('Google 로그인 서비스를 불러올 수 없습니다.');
      setIsLoading(false);
    }
  };

//   역할:
// 사용자가 로그인 버튼 누르면 구글 로그인 팝업을 띄우는 함수.
// window.google.accounts.id.prompt() 가 구글 로그인 UI를 띄워줌.
// 주의점:
// 이 함수 호출 전에 반드시 1번에서 window.google.accounts.id.initialize()가 완료되어 있어야 함.
// 만약 구글 API 스크립트가 아직 안 불러와졌거나 초기화 안 됐다면 오류 처리 필요.

  return (
    <>
      <LoginContainer>
        <LoginCard>
          {/* 제목 */}
          <Title>LOGIN</Title>
          <Subtitle>Google 계정으로 로그인해주세요</Subtitle>

          {/* 에러 메시지 */}
          {error && <ErrorMessage>{error}</ErrorMessage>}

          {/* Google 로그인 버튼 */}
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
          {/* 역할:
            로그인 트리거 역할. 버튼 클릭 시 3번 함수가 실행됨.
            로딩 상태에 따라 버튼 비활성화 및 텍스트 변경 처리. */}
        </LoginCard>
      </LoginContainer>
    </>
  );
};

export default LoginPage;