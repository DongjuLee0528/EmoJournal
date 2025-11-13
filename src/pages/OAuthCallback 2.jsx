// src/pages/OAuthCallback.jsx
import React, { useEffect, useState } from "react";
import Header from '../components/Header';
import Footer from '../components/Footer';

const OAuthCallback = () => {
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [progress, setProgress] = useState('Verifying authentication code...');

  // It's good practice to keep the API base URL in a central config or .env file
  const API_BASE_URL = 'https://emojournal.djloghub.com/api';

  useEffect(() => {
    const processAuth = async () => {
      const urlParams = new URLSearchParams(window.location.search);
      const code = urlParams.get('code');
      const errorParam = urlParams.get('error');
      const state = urlParams.get('state');

      // It's cleaner to remove the used params from the URL
      window.history.replaceState({}, document.title, window.location.pathname);
      
      const savedState = sessionStorage.getItem('oauth_state');
      // Clean up session storage after retrieving the state
      sessionStorage.removeItem('oauth_state');

      if (errorParam) {
        handleAuthError(new Error(`OAuth Error: ${errorParam}. Please try logging in again.`));
        return;
      }

      if (!state || state !== savedState) {
        handleAuthError(new Error('Security validation failed (Invalid state). Please try logging in again.'));
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
    console.log('Starting authorization code processing:', code);
    try {
      setProgress('Sending authentication information to the server...');

      // ⬇️ PKCE code_verifier 추가
      const codeVerifier = localStorage.getItem('pkce_verifier');

      const response = await fetch(`${API_BASE_URL.replace('/api', '')}/login/oauth2/code/google`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        // 'mode' and 'credentials' are often necessary for cross-origin requests that handle auth
        mode: 'cors', 
        credentials: 'include',
        // ⬇️ 백엔드가 codeVerifier를 기대하는 형태로 전송
        // 만약 서버에서 필드명이 snake_case(`code_verifier`)라면 아래 줄을
        // body: JSON.stringify({ code, code_verifier: codeVerifier })
        // 로 바꿔주세요.
        body: JSON.stringify({ code, redirectUri: "https://emojournal.djloghub.com/oauth/callback", codeVerifier }),
      });

      // The response is not OK, delegate to the error handler
      if (!response.ok) {
        await handleErrorResponse(response);
        return; // Important to stop execution here
      }

      setProgress('Verifying user information...');
      const data = await response.json();
      console.log('✅ Server response successful:', data);

      // Check for a success flag or valid token in the response
      if (data.success === true || data.accessToken || data.jwt) {
        const token = data.accessToken || data.token || data.jwt;
        const userInfo = data.user || data.userInfo;
        
        if (token) {
          localStorage.setItem('accessToken', token);
        }
        if (userInfo) {
          localStorage.setItem('userInfo', JSON.stringify(userInfo));
        }
        console.log('✅ Access Token and User Info stored successfully.');
        
        setProgress('Login complete! Redirecting to the main page...');
        setTimeout(() => {
          window.location.href = '/MainPage';
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
  
  const handleErrorResponse = async (response) => {
    // This function now centralizes creating a detailed error to be thrown
    const status = response.status;
    let serverMessage = '';
    try {
      // Try to get more detailed error info from the server's response body
      const errorData = await response.json();
      serverMessage = errorData.message || errorData.error || JSON.stringify(errorData);
    } catch (e) {
      serverMessage = await response.text(); // Fallback to plain text
    }

    console.error(`Server Error [${status}]:`, serverMessage);

    let userMessage = '';
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
    // Throw an error that will be caught by the catch block in handleAuthorizationCode
    throw new Error(`${userMessage} (Server says: ${serverMessage})`);
  };

  const handleAuthError = (error) => {
    let errorMessage = error.message || 'An unknown error occurred.';
    
    // Make network error messages more user-friendly
    if (error.message.includes('Failed to fetch')) {
      errorMessage = 'Cannot connect to the server. Please check your network connection and if the server is running.';
    }
    
    setError(errorMessage);
    setIsLoading(false);
  };
  
  const handleRetryLogin = () => {
    window.location.href = '/';
  };

  return (
    <>
      <Header />
      <div style={{ minHeight: '85vh', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'system-ui, -apple-system, sans-serif', padding: '1rem' }}>
        <div style={{ textAlign: 'center', maxWidth: '600px' }}>
          {isLoading && (
            <>
              <div style={{ margin: '0 auto 20px', width: '40px', height: '40px', border: '4px solid #f3f3f3', borderTop: '4px solid #3498db', borderRadius: '50%', animation: 'spin 1s linear infinite' }}></div>
              <h1 style={{ fontSize: '1.5rem', color: '#374151' }}>Processing Login</h1>
              <p style={{ color: '#6b7280' }}>{progress}</p>
            </>
          )}
          {error && (
            <div style={{ background: '#fee2e2', padding: '1.5rem', borderRadius: '16px', border: '1px solid #fecaca' }}>
              <h1 style={{ fontSize: '1.5rem', color: '#b91c1c', margin: '0 0 1rem 0' }}>Login Failed</h1>
              {/* Providing detailed error for easier debugging */}
              <p style={{ color: '#dc2626', wordBreak: 'break-word', whiteSpace: 'pre-wrap', background: '#fff1f2', padding: '1rem', borderRadius: '8px', fontFamily: 'monospace', fontSize: '0.9rem' }}>
                {error}
              </p>
              <button onClick={handleRetryLogin} style={{ marginTop: '1.5rem', background: '#dc2626', color: 'white', fontWeight: '600', padding: '0.75rem 1.5rem', borderRadius: '12px', border: 'none', cursor: 'pointer' }}>
                Try Again
              </button>
            </div>
          )}
        </div>
        <style>{`
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