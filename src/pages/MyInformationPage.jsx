// MyInformationPage.js
import React from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import BackGround from '../css/background.module.css';
import Footer from '../components/Footer';
import styles from '../css/MyInformationPage.module.css';

const MyInformationPage = () => {
    const navigate = useNavigate();
    
    const handleEditClick = () => {
        // 수정 버튼 클릭 시 처리할 로직
        console.log('수정 버튼 클릭됨');
        // navigate('/edit-profile'); // 예시: 수정 페이지로 이동
    };

    return (
        <>
            <Header />
            <div className={styles.container}>
                <div className={styles.profileCard}>
                    <div className={styles.profileHeader}>
                        <div className={styles.profileAvatar}>
                            <div className={styles.avatarCircle}>
                                <span className={styles.avatarEmoji}></span>
                            </div>
                        </div>
                        <h1 className={styles.greeting}>안녕하세요 양하진님</h1>
                    </div>
                    
                    <div className={styles.profileInfo}>
                        <div className={styles.infoItem}>
                            <span className={styles.infoText}>양하진 [ 닉네임 : 양상보 ]</span>
                        </div>
                        
                        <div className={styles.infoItem}>
                            <span className={styles.infoText}>생년월일 : 2002.03.12</span>
                        </div>
                        
                    <div className={styles.infoItem}>
                        <span className={styles.infoText}>
                            성별 : 여
                            <span style={{marginLeft: '80px'}}>MBTI : ISTP</span>
                        </span>
                    </div>
                        
                        <div className={styles.infoItem}>
                            <span className={styles.infoText}>가입한 날 : 2025.06.25 (일기 쓴지 7일 째)</span>
                        </div>
                    </div>
                    
                    <div className={styles.profileFooter}>
                        <button className={styles.editButton} onClick={handleEditClick}>
                            수정
                        </button>
                    </div>
                </div>
            </div>
            <Footer />
        </>
    );
};

export default MyInformationPage;
