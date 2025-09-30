// src/pages/LoginPage.jsx
import React, { useState } from 'react';
import Header from '../components/Header';
import Footer from '../components/Footer';

/** ===== PKCE 유틸 ===== */
function base64UrlEncode(arrayBuffer) {
  const bytes = new Uint8Array(arrayBuffer);
  let str = '';
  for (let i = 0; i < bytes.byteLength; i++) str += String.fromCharCode(bytes[i]);
  return btoa(str).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}
async function generateCodeVerifier() {
  const random = crypto.getRandomValues(new Uint8Array(64));
  return base64UrlEncode(random);
}
async function generateCodeChallenge(verifier) {
  const enc = new TextEncoder();
  const digest = await crypto.subtle.digest('SHA-256', enc.encode(verifier));
  return base64UrlEncode(digest);
}

/** ===== 환경별 설정 ===== */
const GOOGLE_AUTH_URL = 'https://accounts.google.com/o/oauth2/v2/auth';
const CLIENT_ID = '639506784430-mvf0oth3lt0jc4nab5dbjq18ki7nggsv.apps.googleusercontent.com';
const REDIRECT_URI = process.env.NODE_ENV === 'production'
  ? 'https://emojournal.djloghub.com/oauth/callback'   // ✅ 구글 콘솔 등록값과 반드시 동일
  : 'http://localhost:3000/oauth/callback';

const LoginPage = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleGoogleLogin = async () => {
    setIsLoading(true);
    setError('');

    try {
      // 1) CSRF 방지용 state 생성 및 저장
      const state = crypto.randomUUID();
      sessionStorage.setItem('oauth_state', state);

      // 2) PKCE 생성 (code_verifier 저장, code_challenge 전송)
      const codeVerifier = await generateCodeVerifier();
      const codeChallenge = await generateCodeChallenge(codeVerifier);
      localStorage.setItem('pkce_verifier', codeVerifier);
      sessionStorage.setItem('redirect_uri', REDIRECT_URI);

      // 3) 인증 파라미터 구성
      const params = new URLSearchParams({
        response_type: 'code',
        client_id: CLIENT_ID,
        redirect_uri: REDIRECT_URI,
        scope: [
          'openid',
          'email',
          'profile',
          'https://www.googleapis.com/auth/calendar', // 필요 없으면 제거
        ].join(' '),
        state,
        prompt: 'consent',
        access_type: 'offline',
        include_granted_scopes: 'true',
        code_challenge: codeChallenge,
        code_challenge_method: 'S256',
      });

      // 4) 구글 인증 페이지로 이동
      window.location.href = `${GOOGLE_AUTH_URL}?${params.toString()}`;
    } catch (e) {
      console.error('OAuth 시작 실패:', e);
      setError('Google 로그인 시작 중 오류가 발생했습니다.');
      setIsLoading(false);
    }
  };

  return (
    <>
      <Header />
      <div style={{ minHeight: '85vh', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
        <div style={{ background: 'white', padding: '2rem', borderRadius: 16, boxShadow: '0 10px 30px rgba(0,0,0,.1)', textAlign: 'center' }}>
          <h2 style={{ marginBottom: 12 }}>Google로 로그인</h2>
          {error && (
            <div style={{ background: '#fee2e2', color: '#991b1b', padding: '0.75rem 1rem', borderRadius: 8, marginBottom: 12 }}>
              {error}
            </div>
          )}
          <button
            onClick={handleGoogleLogin}
            disabled={isLoading}
            style={{
              background: '#4285F4', color: '#fff', border: 'none', padding: '0.9rem 1.2rem',
              borderRadius: 10, cursor: 'pointer', fontWeight: 600, minWidth: 220
            }}
          >
            {isLoading ? '처리 중…' : 'Google 계정으로 계속'}
          </button>
        </div>
      </div>
      <Footer />
    </>
  );
};

export default LoginPage;
