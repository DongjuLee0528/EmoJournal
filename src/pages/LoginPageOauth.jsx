// src/pages/LoginPage.jsx
import React, { useState } from 'react';
import Header from '../components/Header';
import Footer from '../components/Footer';

const LoginPage = () => {
  // isLoading is useful if there were other async actions, but here it's very brief.
  // We'll keep it for good UX on the button.
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleGoogleLogin = () => {
    setIsLoading(true);
    setError('');
    
    try {
      // 1. Define Google OAuth 2.0 Parameters
      const oauthConfig = {
        clientId: "639506784430-mvf0oth3lt0jc4nab5dbjq18ki7nggsv.apps.googleusercontent.com",
        redirectUri: "https://emojournal.djloghub.com/oauth/callback", // Must match Google Console EXACTLY
        scope: "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/calendar",
        responseType: "code",
        prompt: 'consent',     // Forces the consent screen to appear on every login attempt.
        accessType: 'offline', // Required to get a refresh_token for long-term access.
      };

      // 2. Generate a secure random state for CSRF protection
      const state = crypto.randomUUID();
      sessionStorage.setItem('oauth_state', state);
      
      // 3. Construct the Authorization URL
      const authUrl = new URL('https://accounts.google.com/o/oauth2/v2/auth');
      authUrl.searchParams.set('response_type', oauthConfig.responseType);
      authUrl.searchParams.set('client_id', oauthConfig.clientId);
      authUrl.searchParams.set('redirect_uri', oauthConfig.redirectUri);
      authUrl.searchParams.set('scope', oauthConfig.scope);
      authUrl.searchParams.set('prompt', oauthConfig.prompt);
      authUrl.searchParams.set('access_type', oauthConfig.accessType);
      authUrl.searchParams.set('state', state);
      
      console.log('Redirecting to Google OAuth URL:', authUrl.toString());
      
      // 4. Redirect the user
      window.location.href = authUrl.toString();
      
    } catch (err) {
      console.error('Failed to initiate OAuth login:', err);
      setError('Could not start the Google login process. Please try again.');
      setIsLoading(false);
    }
  };

  // The JSX for the component remains the same as it is well-styled.
  // ... (Your existing JSX code for the login page) ...
  return (
    <>
      <Header />
      <div style={{ minHeight: '85vh', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
        {/* ... Paste your existing login form JSX here ... */}
        {/* For brevity, I'm just showing the button part that uses the logic */}
        <button onClick={handleGoogleLogin} disabled={isLoading}>
          {/* ... Your button content (SVG, text, spinner) ... */}
          {isLoading ? 'Processing...' : 'Login with Google'}
        </button>
      </div>
      <Footer />
    </>
  );
};

export default LoginPage;