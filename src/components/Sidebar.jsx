import React from 'react';
import styled, { keyframes } from 'styled-components';
import { useLocation, Link } from 'react-router-dom';

// 애니메이션 정의 - 가벼운 버전
const slideInLeft = keyframes`
  0% {
    transform: translateX(-100%);
    opacity: 0;
  }
  100% {
    transform: translateX(0);
    opacity: 1;
  }
`;

const slideOutLeft = keyframes`
  0% {
    transform: translateX(0);
    opacity: 1;
  }
  100% {
    transform: translateX(-100%);
    opacity: 0;
  }
`;

const fadeIn = keyframes`
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
`;

const fadeInUp = keyframes`
  0% {
    opacity: 0;
    transform: translateY(10px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
`;

const SidebarOverlay = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.2);
  z-index: 1500;
  animation: ${fadeIn} 0.2s ease;
  opacity: ${props => props.isOpen ? 1 : 0};
  visibility: ${props => props.isOpen ? 'visible' : 'hidden'};
`;

const SidebarWrapper = styled.div`
  width: 220px;
  height: 100vh;
  background: #ffeef8;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 1600;
  animation: ${props => props.isOpen ? slideInLeft : slideOutLeft} 0.25s ease;
  transform: translateX(${props => props.isOpen ? '0' : '-100%'});
  border-radius: 0 15px 15px 0;
  
  @media (max-width: 768px) {
    width: 240px;
  }
  
  @media (max-width: 480px) {
    width: 260px;
  }
`;

const CloseButton = styled.button`
  position: absolute;
  top: 20px;
  right: 20px;
  background: none;
  border: none;
  font-size: 24px;
  color: #bbb;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  opacity: 0.6;
  
  &:hover {
    color: #ff91a4;
    opacity: 1;
  }
`;

const Logo = styled.div`
  font-family: '온글잎 의연체', sans-serif;
  font-size: 30px;
  text-align: center;
  margin: 40px 0 20px;
  color: #333;
  cursor: pointer;
  transition: color 0.2s ease;
  animation: ${fadeInUp} 0.3s ease 0.1s both;
  
  &:hover {
    color: #ff6b6b;
  }
  
  @media (max-width: 480px) {
    font-size: 22px;
    margin: 50px 0 20px;
  }
`;

const MenuList = styled.ul`
  list-style: none;
  padding: 0 10px;
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
`;

const StyledLink = styled(Link)`
  text-decoration: none;
  display: block;
`;

const MenuItem = styled.li`
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 10px;
  cursor: pointer;
  color: ${props => (props.active ? 'white' : '#333')};
  background: ${props => props.active 
    ? '#ff91a4' 
    : 'transparent'};
  font-weight: ${props => (props.active ? 'bold' : 'normal')};
  font-family: '온글잎 의연체', sans-serif;
  font-size: 24px;
  transition: all 0.2s ease;
  animation: ${fadeInUp} 0.2s ease ${props => (props.index + 1) * 0.05}s both;
  
  &:hover {
    background: ${props => props.active ? '#ff6b6b' : '#ffb3ba'};
    color: white;
  }
  
  @media (max-width: 480px) {
    padding: 14px 16px;
    font-size: 16px;
  }
`;

const BottomText = styled(Link)`
  text-decoration: none;
  text-align: center;
  font-size: 20px;
  font-family: '온글잎 의연체', sans-serif;
  padding: 20px;
  color: #666;
  transition: color 0.2s ease;
  animation: ${fadeInUp} 0.3s ease 0.3s both;
  cursor: pointer;
  
  &:hover {
    color: #ff6b6b;
  }
  
  small {
    font-size: 16px;
    opacity: 0.8;
    display: block;
    margin-top: 5px;
    color: #888;
  }
`;

const menus = [
  { path: '/MainPage', label: '캘린더 페이지' },
  { path: '/DiaryWritingPage', label: '일기 작성' },
  { path: '/DiaryListPage', label: '일기 목록' },
  { path: '/MyInformationPage', label: '내 정보' },
  { path: '/DemoInformationPage', label: 'DEMO 내 정보' },
  { path: '/LoginPageOauth', label: '로그인' },
];

const SideBar = ({ isOpen, onClose }) => {
  const location = useLocation();
  
  const handleOverlayClick = (e) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };
  
  const handleMenuClick = () => {
    // 메뉴 클릭 시 사이드바 닫기 (모바일에서 유용)
    if (window.innerWidth <= 768) {
      onClose();
    }
  };

  return (
    <>
      <SidebarOverlay isOpen={isOpen} onClick={handleOverlayClick} />
      <SidebarWrapper isOpen={isOpen}>
        <CloseButton onClick={onClose} aria-label="사이드바 닫기">
          ×
        </CloseButton>
        
        <div>
          <Logo>EMOJOURNAL</Logo>
          <MenuList>
            {menus.map((menu, index) => (
              <StyledLink 
                to={menu.path} 
                key={menu.path}
                onClick={handleMenuClick}
              >
                <MenuItem 
                  active={location.pathname === menu.path}
                  index={index}
                >
                  {menu.label}
                </MenuItem>
              </StyledLink>
            ))}
          </MenuList>
        </div>
        
        <BottomText to="/AboutPage" onClick={handleMenuClick}>
          EmoJournal<br />
          <small>My Mood Diary</small>
        </BottomText>
      </SidebarWrapper>
    </>
  );
};

export default SideBar;
