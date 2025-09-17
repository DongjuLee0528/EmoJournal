// src/pages/OAuthCallback.jsx
import React, { useEffect, useState } from "react";
import Header from '../components/Header';
import Footer from '../components/Footer';

const OAuthCallback = () => {
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [progress, setProgress] = useState('Verifying authentication code...');

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

      if (code) {
        await handleAuthorizationCode(code);
      } else {
        handleAuthError(new Error('Authentication code was not received.'));
      }
    };

    processAuth();
  }, []);

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
        return; // Important to stop execution here
      }

      setProgress('사용자 정보 확인 중...');
      
      // 응답 데이터 파싱
      const data = await response.json();
      console.log('✅ Server response successful:', data);

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
        // If the server responds 200 OK but the operation failed logically
        throw new Error(data.message || 'Login processing failed. The server response was invalid.');
      }
    } catch (error) {
      // Catch fetch errors (network issues) or errors thrown from our logic
      console.error('❌ Authentication processing error:', error);
      handleAuthError(error);
    }
  };

  // 에러 응답 상세 처리
  const handleErrorResponse = async (response) => {
    // This function now centralizes creating a detailed error to be thrown
    const status = response.status;
    let errorText = '';
    
    try {
      // Try to get more detailed error info from the server's response body
      const errorData = await response.json();
      serverMessage = errorData.message || errorData.error || JSON.stringify(errorData);
    } catch (e) {
      serverMessage = await response.text(); // Fallback to plain text
    }

    let errorMessage = '';
    
    switch (status) {
      case 400:
        userMessage = 'Invalid Request: The authentication code may be expired or invalid. Please try again.';
        break;
      case 401:
        userMessage = 'Authentication Failed: Please check your Google OAuth settings on the server.';
        break;
      case 403:
        userMessage = 'Forbidden: You do not have permission to perform this action.';
        break;
      case 404:
        userMessage = 'API Not Found: The endpoint /login/oauth2/code/google could not be found.';
        break;
      case 500:
        userMessage = 'Internal Server Error: The server encountered a problem. Please contact support or try again later.';
        break;
      default:
        userMessage = `An unexpected server error occurred (${status}).`;
    }
    
    throw new Error(errorMessage);
  };

  const handleAuthError = (error) => {
    let errorMessage = error.message || '알 수 없는 오류가 발생했습니다.';
    
    if (error.message.includes('Failed to fetch')) {
      errorMessage = 'Cannot connect to the server. Please check your network connection and if the server is running.';
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
  );
};

export default OAuthCallback;
