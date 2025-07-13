// 내 정보 
import React from 'react'
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import BackGround from '../css/background.module.css'
import Footer from '../components/Footer';

import styles from '../css/MyInformationPage.module.css'
const MyInformationPage =()=>{
    const navigate = useNavigate();
    return(
        <>   
            <Header/>
            <div>
                <p>MyInformationPage</p>
            </div>
            <Footer/>
        </>
    );
};

export default MyInformationPage;