// 일기 작성 페이지

import React from 'react'
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import BackGround from '../css/background.module.css'
import Footer from '../components/Footer';

import styles from '../css/DiaryWritingPage.module.css'
const DiaryWritingPage =()=>{
    const navigate = useNavigate();
    return(
        <>   
            <Header/>
            <div >
                <p>DiaryWritingPage</p>
            </div>
            <Footer/>
        </>
    );
};

export default DiaryWritingPage;