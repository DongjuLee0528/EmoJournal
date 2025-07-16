// h : 78px
import React from 'react'
import styles from '../css/Header.module.css'
import menu from '../image/menu.png'
import profile from '../image/profile.svg'
import { useNavigate } from 'react-router-dom';
const Header =()=>{
    const navigate = useNavigate();
    return(
        
        <div className={styles.header}>
            <div className={styles.menuBar}>
                <img src={menu} alt="메뉴바"/>
            </div>
            <div className={styles.logo} onClick={() => navigate('/')}>
                <div className={styles.mainText}>
                    <p >EMOJOURNAL</p>
                </div>
                <div className={styles.subText}>
                    <p >My Mood Diary</p>
                </div>
                
            </div>
            <div className={styles.profileBar}>
                <img src={profile} alt="프로필바"/>
            </div>
        </div>
    );
};

export default Header;
