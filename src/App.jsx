// App.jsx
import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import GlobalStyle from './styles/GlobalStyle';

import Header from './components/Header'; // 헤더 추가
import Footer from './components/Footer';

import MainPage from './pages/MainPage';
import DiaryListPage from './pages/DiaryListPage';
import DiaryWritingPage from './pages/DiaryWritingPage';
import LoginPage from './pages/LoginPage';
import LoginPageTest from './pages/LoginPageTest';
import MyInformationPageTest from './pages/MyInformationPageTest';
import MyInformationPage from './pages/MyInformationPage';
import MovePage from './pages/MovePage';


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
          <Route path="/LoginPage" element={<LoginPage />} />
          <Route path="/LoginPageTest" element={<LoginPageTest />} />
          <Route path="/MyInformationPageTest" element={<MyInformationPageTest />} />
          <Route path="/MyInformationPage" element={<MyInformationPage />} />

        </Routes>
      </div>
      <Footer/>
    </BrowserRouter>
  );
};

export default App;
