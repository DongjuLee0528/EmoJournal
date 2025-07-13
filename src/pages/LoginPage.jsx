//  로그인  
import React from 'react'
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import BackGround from '../css/background.module.css'
import Footer from '../components/Footer';

import styles from '../css/LoginPage.module.css'
const LoginPage =()=>{
    const navigate = useNavigate();
    return(
        <>   
            <Header/>
            <div >
                <p>LoginPage</p>
            </div>
            <Footer/>
        </>
    );
};

export default LoginPage;