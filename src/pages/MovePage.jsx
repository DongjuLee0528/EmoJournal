// src/pages/MovePage.jsx
import React from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';

import Footer from '../components/Footer';
import styles from '../css/MovePage.module.css';

const MovePage = () => {
  const navigate = useNavigate();

  return (
    <>
      <Header />

      <div className={styles.container}>
        <h1 className={styles.title}>이동 페이지</h1>
        <div className={styles.buttonGroup}>
          <button onClick={() => navigate('/MainPage')}>메인 페이지</button>
          <button onClick={() => navigate('/DiaryListPage')}>일기 목록 페이지</button>
          <button onClick={() => navigate('/DiaryWritingPage')}>일기 작성 페이지</button>
          <button onClick={() => navigate('/LoginPage')}>로그인 페이지</button>
          <button onClick={() => navigate('/MyInformationPage')}>내 정보 페이지</button>
        </div>
      </div>

      <Footer />
    </>
  );
};

export default MovePage;
