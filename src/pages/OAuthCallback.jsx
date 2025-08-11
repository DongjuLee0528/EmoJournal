// src/pages/OAuthCallback.jsx
import React, { useEffect, useState } from "react";
import Header from '../components/Header';
import Footer from '../components/Footer';


const OAuthCallback = () => {
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [progress, setProgress] = useState('인증 코드 확인 중...');

  // 서버 API Base URL
  const API_BASE_URL = 'https://emojournal.djloghub.com/api';

  useEffect(() => {
    // URL 파라미터에서 인증 결과 확인
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');
    const errorParam = urlParams.get('error');
    const state = urlParams.get('state');

    // OAuth 에러 처리
    if (errorParam) {
      console.error('OAuth 에러:', errorParam);
      
      let errorMessage = 'Google 로그인 중 오류가 발생했습니다.';
      if (errorParam === 'access_denied') {
        errorMessage = '사용자가 Google 로그인 권한을 거부했습니다.';
      } else if (errorParam === 'invalid_request') {
        errorMessage = '잘못된 OAuth 요청입니다.';
      } else if (errorParam === 'unauthorized_client') {
        errorMessage = '인증되지 않은 클라이언트입니다.';
      }
      
      setError(errorMessage);
      setIsLoading(false);
      return;
    }

    // State 파라미터 검증 (CSRF 방지)
    if (state !== 'login') {
      console.error('State 파라미터 불일치:', state);
      setError('보안 검증 실패: 잘못된 요청입니다.');
      setIsLoading(false);
      return;
    }

    // Authorization Code 처리
    if (code) {
      handleAuthorizationCode(code);
    } else {
      setError('인증 코드를 받지 못했습니다.');
      setIsLoading(false);
    }
  }, []);

  // Authorization Code를 서버에 전송하여 토큰 교환
  const handleAuthorizationCode = async (code) => {
    console.log('Authorization Code 처리 시작:', code);
    
    try {
      setProgress('서버에 인증 정보 전송 중...');
      
      // 서버에 Authorization Code 전송
      const response = await fetch(`${API_BASE_URL}/login/oauth2/code/google`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        mode: 'cors',
        credentials: 'include', // 쿠키 포함
        body: JSON.stringify({
          code: code
        })
      });

      // 응답 상태 확인
      if (!response.ok) {
        await handleErrorResponse(response);
        return;
      }

      setProgress('사용자 정보 확인 중...');
      
      // 응답 데이터 파싱
      const data = await response.json();
      console.log('✅ 서버 응답 성공:', data);

      // 로그인 성공 처리
      if (response.status === 200 || data.success === true) {
        setProgress('로그인 완료! 메인 페이지로 이동합니다...');
        
        // JWT 토큰 저장 (서버에서 제공하는 경우)
        if (data.accessToken || data.token || data.jwt) {
          const token = data.accessToken || data.token || data.jwt;
          localStorage.setItem('accessToken', token);
          console.log('✅ Access Token 저장 완료');
        }

        // 사용자 정보 저장 (필요한 경우)
        if (data.user || data.userInfo) {
          const userInfo = data.user || data.userInfo;
          localStorage.setItem('userInfo', JSON.stringify(userInfo));
          console.log('✅ 사용자 정보 저장 완료');
        }
        
        // 성공 후 메인 페이지로 리다이렉트
        setTimeout(() => {
          window.location.href = '/main';
        }, 1500);
        
      } else {
        throw new Error(data.message || data.error || '로그인 처리 실패');
      }

    } catch (error) {
      console.error('❌ 인증 처리 오류:', error);
      handleAuthError(error);
    }
  };

  // 에러 응답 상세 처리
  const handleErrorResponse = async (response) => {
    const status = response.status;
    let errorText = '';
    
    try {
      errorText = await response.text();
      console.error(`서버 오류 [${status}]:`, errorText);
    } catch (e) {
      console.error('응답 파싱 오류:', e);
    }

    let errorMessage = '';
    
    switch (status) {
      case 400:
        errorMessage = '잘못된 요청: 인증 코드가 만료되었거나 유효하지 않습니다.';
        break;
      case 401:
        errorMessage = '인증 실패: Google OAuth 설정을 확인해주세요.';
        break;
      case 403:
        if (errorText.includes('CSRF') || errorText.includes('Invalid CSRF Token')) {
          errorMessage = 'CSRF 보안 오류: 페이지를 새로고침한 후 다시 시도해주세요.';
        } else {
          errorMessage = '접근 권한이 없습니다. 서버 설정을 확인해주세요.';
        }
        break;
      case 404:
        errorMessage = 'API 엔드포인트를 찾을 수 없습니다: /login/oauth2/code/google';
        break;
      case 500:
        errorMessage = '서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
        break;
      default:
        errorMessage = `서버 오류 (${status}): ${errorText || response.statusText}`;
    }
    
    throw new Error(errorMessage);
  };

  // 인증 에러 처리
  const handleAuthError = (error) => {
    let errorMessage = error.message || '알 수 없는 오류가 발생했습니다.';
    
    if (error.message.includes('Failed to fetch')) {
      errorMessage = '서버에 연결할 수 없습니다. 서버가 실행 중인지 확인해주세요.';
    } else if (error.message.includes('NetworkError')) {
      errorMessage = '네트워크 오류가 발생했습니다. 인터넷 연결을 확인해주세요.';
    }
    
    setError(errorMessage);
    setIsLoading(false);
  };

  // 로그인 페이지로 돌아가기
  const handleRetryLogin = () => {
    // URL 파라미터 정리
    window.history.replaceState({}, document.title, window.location.pathname);
    // 로그인 페이지로 이동
    window.location.href = '/';
  };

  return (
    <>
    <Header />
    <div style={{
      minHeight: '85vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      fontFamily: 'system-ui, -apple-system, sans-serif',
    }}>
      <div style={{
        background: 'rgba(255, 255, 255, 0.95)',
        backdropFilter: 'blur(10px)',
        borderRadius: '24px',
        padding: '2.5rem',
        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
        width: '100%',
        maxWidth: '450px',
        margin: '1rem',
        textAlign: 'center'
      }}>
        
        {isLoading ? (
          // 로딩 상태
          <>
            <div style={{
              width: '50px',
              height: '50px',
              border: '4px solid #f1f5f9',
              borderTop: '4px solid #3b82f6',
              borderRadius: '50%',
              animation: 'spin 1s linear infinite',
              margin: '0 auto 1.5rem'
            }}></div>
            
            <h2 style={{
              fontSize: '1.5rem',
              fontWeight: '600',
              color: '#374151',
              margin: '0 0 1rem 0'
            }}>
              Google 로그인 처리 중
            </h2>
            
            <p style={{
              color: '#6b7280',
              fontSize: '1rem',
              margin: '0',
              lineHeight: '1.5'
            }}>
              {progress}
            </p>
            
            <div style={{
              marginTop: '1.5rem',
              padding: '1rem',
              background: '#eff6ff',
              borderRadius: '12px',
              border: '1px solid #dbeafe'
            }}>
              <p style={{
                color: '#1e40af',
                fontSize: '0.85rem',
                margin: '0',
                lineHeight: '1.4'
              }}>
                잠시만 기다려주세요.<br/>
                Google에서 받은 인증 정보를 처리하고 있습니다.
              </p>
            </div>
          </>
        ) : error ? (
          // 에러 상태
          <>
            <div style={{
              width: '50px',
              height: '50px',
              background: '#fee2e2',
              borderRadius: '50%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              margin: '0 auto 1.5rem'
            }}>
              <svg width="24" height="24" fill="#dc2626" viewBox="0 0 24 24">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
              </svg>
            </div>
            
            <h2 style={{
              fontSize: '1.5rem',
              fontWeight: '600',
              color: '#dc2626',
              margin: '0 0 1rem 0'
            }}>
              로그인 실패
            </h2>
            
            <div style={{
              background: '#fee2e2',
              color: '#991b1b',
              padding: '1.25rem',
              borderRadius: '12px',
              marginBottom: '1.5rem',
              fontSize: '0.9rem',
              lineHeight: '1.5',
              border: '1px solid #fecaca'
            }}>
              {error}
            </div>
            
            <button
              onClick={handleRetryLogin}
              style={{
                background: '#3b82f6',
                color: 'white',
                fontWeight: '600',
                padding: '1rem 2rem',
                borderRadius: '12px',
                border: 'none',
                cursor: 'pointer',
                fontSize: '1rem',
                transition: 'all 0.2s ease-in-out'
              }}
              onMouseEnter={(e) => {
                e.target.style.background = '#2563eb';
                e.target.style.transform = 'translateY(-1px)';
              }}
              onMouseLeave={(e) => {
                e.target.style.background = '#3b82f6';
                e.target.style.transform = 'translateY(0)';
              }}
            >
              다시 로그인하기
            </button>
          </>
        ) : (
          // 성공 상태 (일시적으로 표시)
          <>
            <div style={{
              width: '50px',
              height: '50px',
              background: '#dcfce7',
              borderRadius: '50%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              margin: '0 auto 1.5rem'
            }}>
              <svg width="24" height="24" fill="#16a34a" viewBox="0 0 24 24">
                <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
              </svg>
            </div>
            
            <h2 style={{
              fontSize: '1.5rem',
              fontWeight: '600',
              color: '#16a34a',
              margin: '0 0 1rem 0'
            }}>
              로그인 성공!
            </h2>
            
            <p style={{
              color: '#6b7280',
              fontSize: '1rem',
              margin: '0'
            }}>
              메인 페이지로 이동합니다...
            </p>
          </>
        )}
      </div>
      
      <style jsx>{`
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
      `}</style>
    </div>
    <Footer />
    </>
  );
};

export default OAuthCallback;