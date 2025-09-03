// src/pages/LoginPage.jsx
import React, { useState } from 'react';
import Header from '../components/Header';
import Footer from '../components/Footer';

const LoginPage = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleGoogleLogin = () => {
    setIsLoading(true);
    setError('');
    
    try {
      // 1. Google OAuth 2.0 설정
      const oauthConfig = {
        clientId: "639506784430-mvf0oth3lt0jc4nab5dbjq18ki7nggsv.apps.googleusercontent.com",
        redirectUri: "https://emojournal.djloghub.com/oauth/callback", // Google Console와 정확히 일치해야 함
        scope: "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/calendar",
        responseType: "code",
        prompt: 'consent', // 매번 동의 화면이 나타나도록 강제
        accessType: 'offline', // refresh_token을 받기 위해 필요
      };

      // 2. CSRF 보호를 위한 보안 랜덤 state 생성
      const state = crypto.randomUUID();
      sessionStorage.setItem('oauth_state', state);
      
      // 3. Google OAuth 2.0 Authorization URL 생성
      const authUrl = new URL('https://accounts.google.com/o/oauth2/v2/auth');
      authUrl.searchParams.set('response_type', oauthConfig.responseType);
      authUrl.searchParams.set('client_id', oauthConfig.clientId);
      authUrl.searchParams.set('redirect_uri', oauthConfig.redirectUri);
      authUrl.searchParams.set('scope', oauthConfig.scope);
      authUrl.searchParams.set('prompt', oauthConfig.prompt);
      authUrl.searchParams.set('access_type', oauthConfig.accessType);
      authUrl.searchParams.set('state', state);
      
      console.log('생성된 state:', state);
      console.log('Google OAuth URL로 리다이렉트:', authUrl.toString());
      
      // 4. Google 로그인 페이지로 리다이렉트
      window.location.href = authUrl.toString();
      
    } catch (err) {
      console.error('OAuth 로그인 시작 실패:', err);
      setError('Google 로그인을 시작할 수 없습니다. 다시 시도해주세요.');
      setIsLoading(false);
    }
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
          maxWidth: '420px',
          margin: '1rem'
        }}>
          {/* 제목 */}
          <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
            <h1 style={{
              fontSize: '2.5rem',
              fontWeight: 'bold',
              color: '#374151',
              margin: '0 0 0.5rem 0'
            }}>
              LOGIN
            </h1>
            
            <p style={{
              color: '#6b7280',
              fontSize: '1.1rem',
              margin: '0'
            }}>
              Google 계정으로 로그인해주세요
            </p>
          </div>

          {/* 에러 메시지 */}
          {error && (
            <div style={{
              background: '#fee2e2',
              color: '#dc2626',
              padding: '1rem',
              borderRadius: '12px',
              marginBottom: '1.5rem',
              fontSize: '0.9rem',
              textAlign: 'center',
              border: '1px solid #fecaca'
            }}>
              {error}
            </div>
          )}

          {/* Google OAuth 로그인 버튼 */}
          <button
            onClick={handleGoogleLogin}
            disabled={isLoading}
            style={{
              width: '100%',
              background: isLoading ? '#f9fafb' : 'white',
              color: '#374151',
              fontWeight: '600',
              padding: '1rem 1.5rem',
              borderRadius: '16px',
              border: '2px solid #e5e7eb',
              cursor: isLoading ? 'not-allowed' : 'pointer',
              transition: 'all 0.2s ease-in-out',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '0.75rem',
              fontSize: '1.1rem',
              opacity: isLoading ? 0.7 : 1,
              transform: 'scale(1)',
            }}
            onMouseEnter={(e) => {
              if (!isLoading) {
                e.target.style.background = '#f8fafc';
                e.target.style.borderColor = '#d1d5db';
                e.target.style.transform = 'scale(1.02)';
                e.target.style.boxShadow = '0 10px 25px -5px rgba(0, 0, 0, 0.1)';
              }
            }}
            onMouseLeave={(e) => {
              if (!isLoading) {
                e.target.style.background = 'white';
                e.target.style.borderColor = '#e5e7eb';
                e.target.style.transform = 'scale(1)';
                e.target.style.boxShadow = 'none';
              }
            }}
          >
            {/* Google 아이콘 */}
            <svg width="22" height="22" viewBox="0 0 24 24">
              <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
              <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
              <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
              <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
            </svg>
            
            {isLoading ? (
              <>
                <div style={{
                  width: '16px',
                  height: '16px',
                  border: '2px solid #f3f3f3',
                  borderTop: '2px solid #3498db',
                  borderRadius: '50%',
                  animation: 'spin 1s linear infinite'
                }}></div>
                로그인 중...
              </>
            ) : (
              'Google로 로그인'
            )}
          </button>

          {/* OAuth 정보 안내 */}
          <div style={{
            marginTop: '2rem',
            padding: '1.25rem',
            background: '#f8fafc',
            borderRadius: '12px',
            border: '1px solid #e2e8f0'
          }}>
            <h3 style={{
              fontSize: '0.9rem',
              fontWeight: '600',
              color: '#475569',
              margin: '0 0 0.75rem 0',
              textAlign: 'center'
            }}>
              OAuth 2.0 권한 요청
            </h3>
            
            <div style={{
              fontSize: '0.85rem',
              color: '#64748b',
              lineHeight: '1.5',
              textAlign: 'left'
            }}>
              • 기본 프로필 정보 (이름, 이메일)<br/>
              • Google Calendar 전체 접근 권한<br/>
              • 오프라인 액세스 (refresh_token)
            </div>
          </div>

          {/* 서버 정보 */}
          <div style={{
            marginTop: '1rem',
            padding: '1rem',
            background: '#ecfdf5',
            borderRadius: '12px',
            border: '1px solid #d1fae5',
            textAlign: 'center'
          }}>
            <div style={{
              fontSize: '0.85rem',
              color: '#065f46',
              lineHeight: '1.4'
            }}>
              <strong>🔒 보안 연결</strong><br/>
              서버: emojournal.djloghub.com<br/>
              OAuth 콜백: /oauth/callback
            </div>
          </div>
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

export default LoginPage;
