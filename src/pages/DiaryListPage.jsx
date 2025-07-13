// 일기 목록

import React from 'react'
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import BackGround from '../css/background.module.css'
import Footer from '../components/Footer';

import styles from '../css/DiaryListPage.module.css'
const DiaryListPage =()=>{
    const navigate = useNavigate();
    return(
        <>   
            <Header/>
            <div >
                <p>DiaryListPage</p>
            </div>
            <Footer/>
        </>
    );
};

export default DiaryListPage;