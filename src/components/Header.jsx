import React, { memo, useCallback, useEffect, useRef, useState } from 'react';
import styled from 'styled-components';
import { useNavigate } from 'react-router-dom';
import SideBar from './SideBar';

// 상수 정의
const BREAKPOINTS = {
  mobile: '480px',
  tablet: '768px',
  small: '320px'
};

const HEADER_HEIGHT = {
  desktop: '65px',
  tablet: '65px',
  mobile: '60px',
  small: '55px'
};

// Styled-components 정의
const HeaderWrapper = styled.header`
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: ${HEADER_HEIGHT.desktop};
  padding: 0 20px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;

  /* [수정] 배경색 및 블러 효과 제거 */

  /* 스크롤 비율 기준 숨김 애니메이션 */
  transform: translateY(${props => (props.$hidden ? '-100%' : '0')});
  transition: transform 220ms ease;

  @media (max-width: ${BREAKPOINTS.tablet}) {
    padding: 0 15px;
    height: ${HEADER_HEIGHT.tablet};
  }
  
  @media (max-width: ${BREAKPOINTS.mobile}) {
    padding: 0 12px;
    height: ${HEADER_HEIGHT.mobile};
  }
  
  @media (max-width: ${BREAKPOINTS.small}) {
    padding: 0 10px;
    height: ${HEADER_HEIGHT.small};
  }
`;

const TextButton = styled.button`
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 8px 12px;
  border: none;
  background: transparent;
  border-radius: 6px;
  transition: all 0.2s ease;
  font-family: '온글잎 의연체', sans-serif;
  font-size: 28px;
  font-weight: 500;
  color: #333;
  
  &:hover {
    background: rgba(255, 145, 164, 0.1);
    color: #ff91a4;
  }
  
  @media (max-width: ${BREAKPOINTS.tablet}) {
    font-size: 15px;
    padding: 6px 10px;
  }
  
  @media (max-width: ${BREAKPOINTS.mobile}) {
    font-size: 14px;
    padding: 6px 8px;
  }
  
  @media (max-width: ${BREAKPOINTS.small}) {
    font-size: 13px;
    padding: 5px 6px;
  }
`;

const MenuButton = styled(TextButton)``;

const ProfileButton = styled(TextButton)`
  color: ${props => (props.isLoggedIn ? '#ff91a4' : '#666')};
  font-weight: ${props => (props.isLoggedIn ? '600' : '500')};
  
  &:hover {
    background: ${props => (props.isLoggedIn ? 'rgba(255, 145, 164, 0.15)' : 'rgba(102, 102, 102, 0.1)')};
    color: ${props => (props.isLoggedIn ? '#ff6b6b' : '#333')};
  }
`;

const Logo = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: transform 0.2s ease;
  margin-top: 8px;
  
  &:hover {
    transform: translateY(-1px);
  }
  
  &:active {
    transform: translateY(1px);
  }
`;

const MainText = styled.h1`
  font-family: 'Cherry Bomb One', cursive;
  color: rgba(255, 88, 172, 0.67);
  text-shadow: 0px 2px 4px rgba(0, 0, 0, 0.25);
  -webkit-text-stroke-width: 1px;
  -webkit-text-stroke-color: #060606;
  font-size: clamp(28px, 5.5vw, 48px);
  margin: 0;
  padding: 0;
  line-height: 1;
  white-space: nowrap;
  font-weight: normal;
  transition: color 0.2s ease;
`;

const SubText = styled.p`
  font-family: 'Cherry Bomb One', cursive;
  color: rgba(0, 0, 0, 0.83);
  font-size: clamp(12px, 2.5vw, 16px);
  margin: -2px 0 0 0;
  padding: 0;
  line-height: 1;
  white-space: nowrap;
  transition: color 0.2s ease;
`;

// 컴포넌트
const Header = memo(() => {
  const navigate = useNavigate();
  const [isSideBarOpen, setIsSideBarOpen] = useState(false);
  const [hideOnScroll, setHideOnScroll] = useState(false);
  const ticking = useRef(false);

  const isLoggedIn = () => localStorage.getItem('authToken') !== null;
  const loggedIn = isLoggedIn();

  const handleLogoClick = useCallback(() => {
    navigate('/MainPage');
  }, [navigate]);
  
  const handleMenuClick = useCallback(() => {
    setIsSideBarOpen(true);
  }, []);
  
  const handleProfileClick = useCallback(() => {
    if (loggedIn) {
      navigate('/MyInformationPage');
    } else {
      navigate('/LoginPageOauth');
    }
  }, [navigate, loggedIn]);
  
  const handleSideBarClose = useCallback(() => {
    setIsSideBarOpen(false);
  }, []);
  
  const handleLogoKeyDown = useCallback((e) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      handleLogoClick();
    }
  }, [handleLogoClick]);

  useEffect(() => {
    const HIDE_THRESHOLD_PERCENT = 15;

    const calcScrollable = () => {
      const doc = document.documentElement;
      return Math.max(0, doc.scrollHeight - window.innerHeight);
    };

    let maxScrollable = calcScrollable();

    const update = () => {
      const scrollY = window.scrollY || document.documentElement.scrollTop || 0;
      if (maxScrollable <= 0) {
        setHideOnScroll(false);
        return;
      }
      const progress = (scrollY / maxScrollable) * 100;
      setHideOnScroll(progress >= HIDE_THRESHOLD_PERCENT);
    };

    const onScrollOrResize = () => {
      if (ticking.current) return;
      ticking.current = true;
      window.requestAnimationFrame(() => {
        maxScrollable = calcScrollable();
        update();
        ticking.current = false;
      });
    };
    
    maxScrollable = calcScrollable();
    update();

    window.addEventListener('scroll', onScrollOrResize, { passive: true });
    window.addEventListener('resize', onScrollOrResize);

    return () => {
      window.removeEventListener('scroll', onScrollOrResize);
      window.removeEventListener('resize', onScrollOrResize);
    };
  }, []);

  return (
    <>
      <HeaderWrapper role="banner" $hidden={hideOnScroll}>
        <MenuButton 
          onClick={handleMenuClick}
          aria-label="메뉴 열기"
          type="button"
        >
          Menu
        </MenuButton>
        
        <Logo 
          onClick={handleLogoClick}
          onKeyDown={handleLogoKeyDown}
          tabIndex={0}
          role="button"
          aria-label="메인페이지로 이동"
        >
          <MainText>EMOJOURNAL</MainText>
          <SubText>My Mood Diary</SubText>
        </Logo>
        
        <ProfileButton 
          onClick={handleProfileClick}
          aria-label={loggedIn ? '사용자 프로필' : '로그인'}
          type="button"
          isLoggedIn={loggedIn}
        >
          {loggedIn ? 'Profile' : 'Login'}
        </ProfileButton>
      </HeaderWrapper>
      
      {isSideBarOpen && (
        <SideBar 
          isOpen={isSideBarOpen} 
          onClose={handleSideBarClose} 
        />
      )}
    </>
  );
});

Header.displayName = 'Header';

export default Header;
