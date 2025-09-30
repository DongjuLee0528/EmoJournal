// App.jsx
import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'; // <-- Navigate 추가
import GlobalStyle from './styles/GlobalStyle';

import Header from './components/Header';
import Footer from './components/Footer';

import MainPage from './pages/MainPage';
import DiaryListPage from './pages/DiaryListPage';
import DiaryWritingPage from './pages/DiaryWritingPage';
import LoginPageOauth from './pages/LoginPageOauth';
import MyInformationPage from './pages/MyInformationPage';
import DemoInformationPage from './pages/DemoInformationPage';
import OAuthCallback from './pages/OAuthCallback';
import SideBar from './components/SideBar';
import AboutPage from './pages/AboutPage';

const App = () => {
  return (
    <BrowserRouter>
      <GlobalStyle />
      <Header />
      <div style={{ paddingTop: '60px' }}>
        <Routes>
          {/* 메인 */}
          <Route path="/" element={<MainPage />} />
          <Route path="/main" element={<MainPage />} />
          {/* 과거 대문자 경로로 들어오면 소문자 /main 으로 정규화 */}
          <Route path="/MainPage" element={<Navigate to="/main" replace />} />

          {/* 일기 */}
          <Route path="/DiaryListPage" element={<DiaryListPage />} />
          <Route path="/DiaryWritingPage" element={<DiaryWritingPage />} />

          {/* 로그인/OAuth */}
          <Route path="/LoginPageOauth" element={<LoginPageOauth />} />
          <Route path="/oauth/callback" element={<OAuthCallback />} />

          {/* 마이페이지/기타 */}
          <Route path="/MyInformationPage" element={<MyInformationPage />} />
          <Route path="/DemoInformationPage" element={<DemoInformationPage />} />
          <Route path="/AboutPage" element={<AboutPage />} />
          <Route path="/SideBar" element={<SideBar />} />
        </Routes>
      </div>
      <Footer />
    </BrowserRouter>
  );
};

export default App;
