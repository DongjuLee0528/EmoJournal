import React from 'react';
import styled from 'styled-components';
import { useLocation, Link } from 'react-router-dom';

const SidebarOverlay = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1500;
  opacity: ${props => props.isOpen ? 1 : 0};
  visibility: ${props => props.isOpen ? 'visible' : 'hidden'};
  transition: opacity 0.3s ease, visibility 0.3s ease;
`;

const SidebarWrapper = styled.div`
  width: 200px;
  height: 100vh;
  background: linear-gradient(to bottom, #f8d2ec, #e5b9f0);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 1600;
  transform: translateX(${props => props.isOpen ? '0' : '-100%'});
  transition: transform 0.3s ease;
  
  @media (max-width: 768px) {
    width: 250px;
  }
  
  @media (max-width: 480px) {
    width: 280px;
  }
`;

const CloseButton = styled.button`
  position: absolute;
  top: 15px;
  right: 15px;
  background: none;
  border: none;
  font-size: 24px;
  color: #333;
  cursor: pointer;
  padding: 5px;
  line-height: 1;
  transition: color 0.2s ease;
  
  &:hover {
    color: #ffffffff;
  }
`;

const Logo = styled.div`
  font-family: '온글잎 의연체', sans-serif;
  font-size: 30px;
  text-align: center;
  margin: 50px 0 10px;
  color: #000000ff;
  cursor: pointer;
  transition: transform 0.2s ease;
  
  &:hover {
    transform: scale(1.05);
  }
  
  @media (max-width: 480px) {
    font-size: 20px;
    margin: 60px 0 10px;
  }
`;

const MenuList = styled.ul`
  list-style: none;
  padding: 0;
  margin-top: 20px;
`;

const StyledLink = styled(Link)`
  text-decoration: none;
  display: block;
`;

const MenuItem = styled.li`
  display: flex;
  align-items: center;
  padding: 14px 20px;
  cursor: pointer;
  color: ${props => (props.active ? 'white' : '#000')};
  background-color: ${props => (props.active ? '#f0a4cfff' : 'transparent')};
  font-weight: ${props => (props.active ? 'bold' : 'normal')};
  font-family: '온글잎 의연체', sans-serif;
  font-size: 26px;
  transition: all 0.2s ease;
  
  &:hover {
    background-color: #f3a5d3;
    color: white;
    transform: translateX(5px);
  }
  
  @media (max-width: 480px) {
    padding: 16px 20px;
    font-size: 16px;
  }
`;

const BottomText = styled.div`
  text-align: center;
  font-size: 28px;
  font-family: '온글잎 의연체', sans-serif;
  padding: 15px;
  color: #333;
  transition: transform 0.2s ease;
  
  &:hover {
    transform: scale(1.02);
  }
  
  small {
    font-size: 25px;
    opacity: 0.8;
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
            {menus.map(menu => (
              <StyledLink 
                to={menu.path} 
                key={menu.path}
                onClick={handleMenuClick}
              >
                <MenuItem active={location.pathname === menu.path}>
                  {menu.label}
                </MenuItem>
              </StyledLink>
            ))}
          </MenuList>
        </div>
        
        <BottomText>
          EmoJournal<br />
          <small>My Mood Diary</small>
        </BottomText>
      </SidebarWrapper>
    </>
  );
};

export default SideBar;