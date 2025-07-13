import React from 'react'
import { useNavigate } from 'react-router-dom'
import Header from '../components/Header'
import BackGround from '../css/background.module.css'
import Footer from '../components/Footer'

import styles from '../css/MainPage.module.css'
const Mainpage =()=>{
    const navigate = useNavigate();
    return(
        <>   
            <Header/>
            <div className={`${BackGround.background} ${styles.Mainpage}`}>
                <p>Mainpage</p>
            </div>
            <Footer/>
        </>
    );
};

export default Mainpage;
