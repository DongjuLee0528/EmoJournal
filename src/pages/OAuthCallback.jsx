// src/pages/OAuthCallback.jsx
import React, { useEffect, useState } from "react";
import Header from '../components/Header';
import Footer from '../components/Footer';

const OAuthCallback = () => {
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [progress, setProgress] = useState('인증 코드 확인 중...');

  const API_BASE_URL = 'https://emojournal.djloghub.com/api';

  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');
    const errorParam = urlParams.get('error');
    const state = urlParams.get('state');

    // 세션에 저장한 state 값 불러오기
    const savedState = sessionStorage.getItem('oauth_state');

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

    // State 검증 (세션에 저장한 값과 비교)
    if (!state || state !== savedState) {
      console.error('State 파라미터 불일치:', state, '저장된 값:', savedState);
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

  const handleAuthorizationCode = async (code) => {
    console.log('Authorization Code 처리 시작:', code);
    try {
      setProgress('서버에 인증 정보 전송 중...');
      const response = await fetch(`${API_BASE_URL}/login/oauth2/code/google`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        mode: 'cors',
        credentials: 'include',
        body: JSON.stringify({ code })
      });

      if (!response.ok) {
        await handleErrorResponse(response);
        return;
      }

      setProgress('사용자 정보 확인 중...');
      const data = await response.json();
      console.log('✅ 서버 응답 성공:', data);

      if (response.status === 200 || data.success === true) {
        setProgress('로그인 완료! 메인 페이지로 이동합니다...');
        if (data.accessToken || data.token || data.jwt) {
          const token = data.accessToken || data.token || data.jwt;
          localStorage.setItem('accessToken', token);
          console.log('✅ Access Token 저장 완료');
        }
        if (data.user || data.userInfo) {
          const userInfo = data.user || data.userInfo;
          localStorage.setItem('userInfo', JSON.stringify(userInfo));
          console.log('✅ 사용자 정보 저장 완료');
        }
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

  const handleRetryLogin = () => {
    window.history.replaceState({}, document.title, window.location.pathname);
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
        {/* 기존 로딩/성공/실패 UI는 동일 */}
        {/* ... */}
      </div>
      <Footer />
    </>
  );
};

export default OAuthCallback;
