import React, { useState, useEffect } from 'react';

const LoginPage = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  // Google OAuth 2.0 설정 - 운영 서버용
  const clientId = "639506784430-mvf0oth3lt0jc4nab5dbjq18ki7nggsv.apps.googleusercontent.com";
  const redirectUri = "https://emojournal.djloghub.com/oauth/callback";
  const scope = "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/calendar";
  const responseType = "code";

  // 운영 서버 API Base URL
  const API_BASE_URL = 'https://emojournal.djloghub.com';

  // URL에서 authorization code 확인 (콜백 처리)
  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');
    const errorParam = urlParams.get('error');

    if (errorParam) {
      // OAuth 에러 처리
      console.error('OAuth 에러:', errorParam);
      if (errorParam === 'access_denied') {
        setError('Google 로그인 권한이 거부되었습니다.');
      } else {
        setError('Google 로그인 중 오류가 발생했습니다.');
      }
      // URL에서 에러 파라미터 제거
      window.history.replaceState({}, document.title, window.location.pathname);
      return;
    }

    if (code) {
      // Authorization code가 있으면 토큰 교환 처리
      handleAuthorizationCode(code);
      // URL에서 code 파라미터 제거
      window.history.replaceState({}, document.title, window.location.pathname);
    }
  }, []);

  // Authorization Code를 Access Token으로 교환
  const handleAuthorizationCode = async (code) => {
    console.log('Authorization Code 받음:', code);
    setIsLoading(true);
    setError('');

    try {
      // 스프링 백엔드에 authorization code 전송하여 토큰 교환 및 사용자 정보 가져오기
      const response = await fetch(`${API_BASE_URL}/api/v1/auth/google/callback`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        // CORS 설정
        mode: 'cors',
        credentials: 'include', // 쿠키/세션 정보 포함
        body: JSON.stringify({
          authorizationCode: code,
          redirectUri: redirectUri,
          clientId: clientId,
          state: 'login' // CSRF 방지를 위한 state
        })
      });

      // HTTP 상태 코드 확인
      if (!response.ok) {
        const errorText = await response.text();
        console.error(`스프링 서버 오류 [${response.status}]:`, errorText);
        throw new Error(`서버 인증 실패: ${response.status} - ${response.statusText}`);
      }

      const data = await response.json();
      console.log('✅ 스프링 백엔드 응답:', data);

      // 스프링 응답 구조에 따른 처리
      if (data.success === true || data.status === 'SUCCESS' || response.status === 200) {
        // 로그인 성공 - 사용자 정보 및 토큰 저장
        const userData = data.data || data.user || data;
        
        console.log('✅ 로그인 성공 - 사용자 정보:', userData);
        
        // JWT 토큰이 있다면 localStorage에 저장
        if (data.accessToken || data.token || data.jwt) {
          const token = data.accessToken || data.token || data.jwt;
          localStorage.setItem('accessToken', token);
          console.log('✅ JWT 토큰 저장됨');
        }
        
        // Refresh Token 저장 (있는 경우)
        if (data.refreshToken) {
          localStorage.setItem('refreshToken', data.refreshToken);
          console.log('✅ Refresh Token 저장됨');
        }
        
        // 사용자 정보 저장
        if (userData) {
          localStorage.setItem('userInfo', JSON.stringify({
            id: userData.id || userData.userId,
            name: userData.name || userData.displayName,
            email: userData.email,
            picture: userData.picture || userData.profileImage,
            provider: 'google'
          }));
        }
        
        alert(`🎉 로그인 성공!\n이름: ${userData.name || userData.displayName}\n이메일: ${userData.email}`);
        
        // 메인 페이지로 리다이렉트
        setTimeout(() => {
          window.location.href = '/main'; // 또는 원하는 페이지로
        }, 1500);
        
      } else {
        // 스프링에서 실패 응답을 보낸 경우
        throw new Error(data.message || data.error || '인증 처리 실패');
      }

    } catch (error) {
      console.error('❌ 스프링 서버 연동 오류:', error);
      
      // 구체적인 에러 처리
      if (error.message.includes('Failed to fetch')) {
        setError('🔌 서버 연결 실패: emojournal.djloghub.com 서버가 실행 중인지 확인해주세요.');
      } else if (error.message.includes('CORS')) {
        setError('🚫 CORS 오류: 서버에서 CORS 설정을 확인해주세요.');
      } else if (error.message.includes('404')) {
        setError('🔍 API 엔드포인트 없음: /api/v1/auth/google/callback 경로를 확인해주세요.');
      } else if (error.message.includes('400')) {
        setError('📝 잘못된 요청: authorization code가 만료되었거나 잘못되었습니다.');
      } else if (error.message.includes('401')) {
        setError('🔐 인증 실패: Google OAuth 설정을 확인해주세요.');
      } else if (error.message.includes('500')) {
        setError('🛠️ 서버 내부 오류: 스프링 서버 로그를 확인해주세요.');
      } else {
        setError(`❌ 로그인 처리 중 오류: ${error.message}`);
      }
    } finally {
      setIsLoading(false);
    }
  };

  // Google OAuth 로그인 시작 (기존 코드와 동일한 방식)
  const handleGoogleLogin = () => {
    setIsLoading(true);
    setError('');
    
    try {
      // Google OAuth 2.0 Authorization URL 생성
      const url = `https://accounts.google.com/o/oauth2/v2/auth?response_type=${responseType}&client_id=${clientId}&redirect_uri=${encodeURIComponent(redirectUri)}&scope=${encodeURIComponent(scope)}&prompt=consent&access_type=offline`;
      
      console.log('Google OAuth URL로 이동:', url);
      
      // Google 로그인 페이지로 리다이렉트
      window.location.href = url;
      
    } catch (error) {
      console.error('OAuth 로그인 시작 오류:', error);
      setError('Google 로그인을 시작할 수 없습니다.');
      setIsLoading(false);
    }
  };

  // 현재 URL에서 code 파라미터가 있는지 확인
  const urlParams = new URLSearchParams(window.location.search);
  const isCallback = urlParams.has('code') || urlParams.has('error');

  return (
    <div style={{
      minHeight: '85vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      fontFamily: 'system-ui, -apple-system, sans-serif'
    }}>
      <div style={{
        background: 'rgba(255, 255, 255, 0.9)',
        backdropFilter: 'blur(10px)',
        borderRadius: '24px',
        padding: '2rem',
        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
        width: '100%',
        maxWidth: '400px',
        margin: '1rem'
      }}>
        {/* 제목 */}
        <h1 style={{
          fontSize: '2.5rem',
          fontWeight: 'bold',
          color: '#374151',
          textAlign: 'center',
          marginBottom: '0.5rem',
          margin: '0 0 0.5rem 0'
        }}>
          {isCallback && isLoading ? 'LOGIN 처리중...' : 'LOGIN'}
        </h1>
        
        <p style={{
          color: '#6b7280',
          fontSize: '1.2rem',
          textAlign: 'center',
          marginBottom: '2rem',
          margin: '0 0 2rem 0'
        }}>
          {isCallback && isLoading ? 'Google 로그인을 처리하고 있습니다...' : 'Google 계정으로 로그인해주세요'}
        </p>

        {/* 에러 메시지 */}
        {error && (
          <div style={{
            background: '#fee2e2',
            color: '#dc2626',
            padding: '0.75rem',
            borderRadius: '8px',
            marginBottom: '1rem',
            fontSize: '0.875rem',
            textAlign: 'center'
          }}>
            {error}
          </div>
        )}

        {/* 로딩 상태 표시 */}
        {isCallback && isLoading && (
          <div style={{
            background: '#e0f2fe',
            color: '#0277bd',
            padding: '0.75rem',
            borderRadius: '8px',
            marginBottom: '1rem',
            fontSize: '0.875rem',
            textAlign: 'center'
          }}>
            Google에서 받은 인증 코드를 처리하고 있습니다...
          </div>
        )}

        {/* Google OAuth 로그인 버튼 */}
        {!isCallback && (
          <button
            onClick={handleGoogleLogin}
            disabled={isLoading}
            style={{
              width: '100%',
              background: 'white',
              color: '#374151',
              fontWeight: '600',
              padding: '0.75rem 1rem',
              borderRadius: '50px',
              border: '1px solid #d1d5db',
              cursor: isLoading ? 'not-allowed' : 'pointer',
              transition: 'all 0.2s ease-in-out',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '0.75rem',
              fontSize: '1rem',
              opacity: isLoading ? 0.5 : 1,
              transform: isLoading ? 'none' : 'scale(1)',
            }}
            onMouseEnter={(e) => {
              if (!isLoading) {
                e.target.style.background = '#f9fafb';
                e.target.style.transform = 'scale(1.02)';
              }
            }}
            onMouseLeave={(e) => {
              if (!isLoading) {
                e.target.style.background = 'white';
                e.target.style.transform = 'scale(1)';
              }
            }}
          >
            <svg width="20" height="20" viewBox="0 0 24 24">
              <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
              <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
              <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
              <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
            </svg>
            {isLoading ? '로그인 중...' : 'Google로 로그인'}
          </button>
        )}

        {/* 콜백 처리 중일 때 다시 시도 버튼 */}
        {isCallback && !isLoading && error && (
          <button
            onClick={() => {
              setError('');
              window.location.href = '/'; // 로그인 페이지로 돌아가기
            }}
            style={{
              width: '100%',
              background: '#3b82f6',
              color: 'white',
              fontWeight: '600',
              padding: '0.75rem 1rem',
              borderRadius: '50px',
              border: 'none',
              cursor: 'pointer',
              fontSize: '1rem',
              marginTop: '1rem'
            }}
          >
            다시 로그인하기
          </button>
        )}

        {/* 안내 메시지 */}
        <div style={{
          marginTop: '1.5rem',
          padding: '1rem',
          background: '#f3f4f6',
          borderRadius: '8px',
          fontSize: '0.875rem',
          color: '#6b7280',
          textAlign: 'center',
          lineHeight: '1.5'
        }}>
          <strong>OAuth 2.0 Authorization Code Flow</strong><br/>
          • 기본 프로필 정보 (이메일, 이름)<br/>
          • Google Calendar 전체 권한<br/>
          • 오프라인 액세스 (refresh_token)
        </div>

        {/* 운영 서버 정보 */}
        <div style={{
          marginTop: '1rem',
          padding: '1rem',
          background: '#dcfce7',
          borderRadius: '8px',
          fontSize: '0.875rem',
          color: '#166534',
          textAlign: 'center',
          lineHeight: '1.5'
        }}>
          <strong>🌐 운영 서버 연동:</strong><br/>
          POST /api/v1/auth/google/callback<br/>
          서버: emojournal.djloghub.com<br/>
          HTTPS 보안 연결
        </div>
      </div>
    </div>
  );
};

export default LoginPage;