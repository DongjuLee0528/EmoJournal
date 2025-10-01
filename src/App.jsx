// App.jsx
import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import GlobalStyle from './styles/GlobalStyle';

import Header from './components/Header'; // 헤더 추가
import Footer from './components/Footer';

import MainPage from './pages/MainPage';
import DiaryListPage from './pages/DiaryListPage';
import DiaryWritingPage from './pages/DiaryWritingPage';
import LoginPageOauth from './pages/LoginPageOauth'; // oauth 방식 로그인
import MyInformationPage from './pages/MyInformationPage';
import DemoInformationPage from './pages/DemoInformationPage';
import MovePage from './pages/MainPage';
import OAuthCallback from './pages/OAuthCallback';
import SideBar from './components/SideBar';
import AboutPage from './pages/AboutPage'; // About 페이지 추가
import LicensesPage from './pages/LicensesPage';
import TermsPage from './pages/TermsPage';


const App = () => {
  return (
    <BrowserRouter>
      <GlobalStyle/>
      <Header />
      <div style={{ paddingTop: '60px' }}> {/* Header 높이만큼 여백 추가 */}
        <Routes>
          <Route path="/" element={<MovePage />} />
          <Route path="/MainPage" element={<MainPage />} />
          <Route path="/DiaryListPage" element={<DiaryListPage />} />
          <Route path="/DiaryWritingPage" element={<DiaryWritingPage />} />
          <Route path="/LoginPageOauth" element={<LoginPageOauth />} />
          <Route path="/MyInformationPage" element={<MyInformationPage />} />
          <Route path="/DemoInformationPage" element={<DemoInformationPage />} />
          <Route path="/AboutPage" element={<AboutPage />} />
          <Route path="/licenses" element={<LicensesPage />} />
          <Route path="/terms" element={<TermsPage />} />
          
          {/* 사이드바 컴포넌트 추가 */}
          <Route path="/SideBar" element={<SideBar />} />

          {/* 소셜로그인후 콜백 페이지 */}
          <Route path="/oauth/callback" element={<OAuthCallback />} />

        </Routes>
      </div>
      <Footer/>
    </BrowserRouter>
  );
};

export default App;