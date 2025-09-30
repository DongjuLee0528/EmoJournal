import React, { useEffect, useState } from "react";

const OAuthCallback = () => {
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [progress, setProgress] = useState("로그인 처리 중...");

    const API_BASE_URL = 'https://emojournal.djloghub.com/api' // 프로덕션 API URL

    const handleAuthorizationCode = async (code) => {
    console.log('Authorization Code 받음:', code);

    setIsLoading(true);
    setError('');

    try {
      // 스프링 백엔드에 authorization code 전송하여 토큰 교환 및 사용자 정보 가져오기
      const response = await fetch(`${API_BASE_URL}/login/oauth2/code/google`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        // CORS 설정
        mode: 'cors',
        credentials: 'include', // 쿠키/세션 정보 포함
        body: JSON.stringify({
            // authorizationCode 스프링히고 클라이언트랑 이름 매핑해줘야됨 
            // 서버에서는 변수명이 code 로 되어있음 
            // 클라는 authorizationCode 여서 불일치
            // 그래서 임시로 일단은 json 값에 필드를 code 로 바꿔놓음 
            // authorizationCode: code,
          code,
          codeVerifier: localStorage.getItem("pkce_verifier")
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
        
      window.location.href = '/main';

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

  useEffect(() => {
    const url = new URL(window.location.href);
    const code = url.searchParams.get("code");   // ← 백엔드 successHandler가 쿼리로 전달
    const err   = url.searchParams.get("error");   // ← 실패 시 에러 메시지

    console.log("code : " + code)

    if (err) {
      setError(err);
      setIsLoading(false);
      return;
    }

    if (code) {
      handleAuthorizationCode(code);

      setProgress("로그인 완료! 메인 페이지로 이동합니다...");
      // 주소창에서 쿼리 정리
      window.history.replaceState({}, document.title, "/oauth/callback");
      // 메인으로 이동
      // window.location.replace("/main");
    } else {
      setError("토큰이 전달되지 않았습니다. 다시 로그인해 주세요.");
      setIsLoading(false);
    }
  }, []);

  return (
    <div style={{ minHeight: "85vh", display: "flex", alignItems: "center", justifyContent: "center" }}>
      <div style={{ background: "#fff", borderRadius: 24, padding: "2.5rem", width: "100%", maxWidth: 450, textAlign: "center" }}>
        {isLoading ? (
          <>
            <div style={{ width: 50, height: 50, border: "4px solid #f1f5f9", borderTop: "4px solid #3b82f6", borderRadius: "50%", animation: "spin 1s linear infinite", margin: "0 auto 1.5rem" }} />
            <h2 style={{ fontSize: "1.5rem", fontWeight: 600, margin: "0 0 1rem" }}>Google 로그인 처리 중</h2>
            <p style={{ color: "#6b7280" }}>{progress}</p>
          </>
        ) : error ? (
          <>
            <h2 style={{ fontSize: "1.5rem", fontWeight: 600, color: "#dc2626", margin: "0 0 1rem" }}>로그인 실패</h2>
            <div style={{ background: "#fee2e2", color: "#991b1b", padding: "1rem", borderRadius: 12, marginBottom: "1.5rem" }}>
              {error}
            </div>
            <button onClick={() => (window.location.href = "/")} style={{ background: "#3b82f6", color: "#fff", fontWeight: 600, padding: "0.9rem 1.2rem", border: "none", borderRadius: 12, cursor: "pointer" }}>
              다시 로그인하기
            </button>
          </>
        ) : null}
      </div>

      <style>{`
        @keyframes spin { 0% {transform: rotate(0deg)} 100% {transform: rotate(360deg)} }
      `}</style>
    </div>
  );
};

export default OAuthCallback;
