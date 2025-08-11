// import React, { memo, useCallback, useState } from 'react';
// import styled from 'styled-components';
// import { useNavigate } from 'react-router-dom';
// import menu from '../image/menu.png';
// import profile from '../image/profile.svg';
// import SideBar from './SideBar';

// // 상수 정의
// const BREAKPOINTS = {
//   mobile: '480px',
//   tablet: '768px',
//   small: '320px'
// };

// const HEADER_HEIGHT = {
//   desktop: '60px',
//   tablet: '60px',
//   mobile: '55px',
//   small: '50px'
// };

// // Styled-components 정의
// const HeaderWrapper = styled.header`
//   display: flex;
//   justify-content: space-between;
//   align-items: center;
//   height: ${HEADER_HEIGHT.desktop};
//   background-color: #ffffff;
//   border-bottom: 1px solid #e0e0e0;
//   padding: 0 20px;
//   box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
//   position: fixed;
//   top: 0;
//   left: 0;
//   right: 0;
//   z-index: 1000;
  
//   @media (max-width: ${BREAKPOINTS.tablet}) {
//     padding: 0 15px;
//     height: ${HEADER_HEIGHT.tablet};
//   }
  
//   @media (max-width: ${BREAKPOINTS.mobile}) {
//     padding: 0 12px;
//     height: ${HEADER_HEIGHT.mobile};
//   }
  
//   @media (max-width: ${BREAKPOINTS.small}) {
//     padding: 0 10px;
//     height: ${HEADER_HEIGHT.small};
//   }
// `;

// const IconButton = styled.button`
//   display: flex;
//   align-items: center;
//   justify-content: center;
//   cursor: pointer;
//   padding: 8px;
//   border: none;
//   background: transparent;
//   border-radius: 4px;
//   transition: transform 0.15s ease;
  
//   &:hover {
//     transform: scale(1.05);
//   }
  
//   &:active {
//     transform: scale(0.95);
//   }
// `;

// const MenuButton = styled(IconButton)`
//   img {
//     width: 24px;
//     height: 24px;
//     object-fit: contain;
    
//     @media (max-width: ${BREAKPOINTS.tablet}) {
//       width: 22px;
//       height: 22px;
//     }
    
//     @media (max-width: ${BREAKPOINTS.mobile}) {
//       width: 20px;
//       height: 20px;
//     }
    
//     @media (max-width: ${BREAKPOINTS.small}) {
//       width: 18px;
//       height: 18px;
//     }
//   }
// `;

// const ProfileButton = styled(IconButton)`
//   border-radius: 50%;
  
//   img {
//     width: 32px;
//     height: 32px;
//     object-fit: contain;
    
//     @media (max-width: ${BREAKPOINTS.tablet}) {
//       width: 28px;
//       height: 28px;
//     }
    
//     @media (max-width: ${BREAKPOINTS.mobile}) {
//       width: 26px;
//       height: 26px;
//     }
    
//     @media (max-width: ${BREAKPOINTS.small}) {
//       width: 24px;
//       height: 24px;
//     }
//   }
// `;

// const Logo = styled.div`
//   display: flex;
//   flex-direction: column;
//   align-items: center;
//   cursor: pointer;
//   transition: transform 0.2s ease;
  
//   &:hover {
//     transform: translateY(-1px);
//   }
  
//   &:active {
//     transform: translateY(1px);
//   }
// `;

// const MainText = styled.h1`
//   font-family: 'Cherry Bomb One', cursive;
//   color: rgba(255, 86, 139, 0.67);
//   text-shadow: 0px 2px 4px rgba(0, 0, 0, 0.25);
//   -webkit-text-stroke-width: 1px;
//   -webkit-text-stroke-color: #060606;
//   font-size: clamp(20px, 4vw, 36px);
//   margin: 0;
//   padding: 0;
//   line-height: 1;
//   white-space: nowrap;
//   font-weight: normal;
//   transition: color 0.2s ease;
// `;

// const SubText = styled.p`
//   font-family: 'Cherry Bomb One', cursive;
//   color: rgba(0, 0, 0, 0.83);
//   font-size: clamp(10px, 2vw, 13px);
//   margin: -2px 0 0 0;
//   padding: 0;
//   line-height: 1;
//   white-space: nowrap;
//   transition: color 0.2s ease;
// `;

// // 컴포넌트
// const Header = memo(() => {
//   const navigate = useNavigate();
//   const [isSideBarOpen, setIsSideBarOpen] = useState(false);
  
//   // 로그인 상태를 확인하는 함수 (실제 로직에 맞게 수정하세요)
//   const isLoggedIn = () => {
//     // 예시: localStorage에서 토큰 확인
//     return localStorage.getItem('authToken') !== null;
//     // 또는 다른 로그인 상태 확인 로직
//   };
  
//   const handleLogoClick = useCallback(() => {
//     navigate('/MainPage');
//   }, [navigate]);
  
//   const handleMenuClick = useCallback(() => {
//     setIsSideBarOpen(true);
//   }, []);
  
//   const handleProfileClick = useCallback(() => {
//     if (isLoggedIn()) {
//       navigate('/MyInformationPage');
//     } else {
//       navigate('/LoginPageOauth');
//     }
//   }, [navigate]);
  
//   const handleSideBarClose = useCallback(() => {
//     setIsSideBarOpen(false);
//   }, []);
  
//   const handleLogoKeyDown = useCallback((e) => {
//     if (e.key === 'Enter' || e.key === ' ') {
//       e.preventDefault();
//       handleLogoClick();
//     }
//   }, [handleLogoClick]);
  
//   return (
//     <>
//       <HeaderWrapper role="banner">
//         <MenuButton 
//           onClick={handleMenuClick}
//           aria-label="메뉴 열기"
//           type="button"
//         >
//           <img src={menu} alt="" />
//         </MenuButton>
        
//         <Logo 
//           onClick={handleLogoClick}
//           onKeyDown={handleLogoKeyDown}
//           tabIndex={0}
//           role="button"
//           aria-label="메인페이지로 이동"
//         >
//           <MainText>EMOJOURNAL</MainText>
//           <SubText>My Mood Diary</SubText>
//         </Logo>
        
//         <ProfileButton 
//           onClick={handleProfileClick}
//           aria-label="사용자 프로필"
//           type="button"
//         >
//           <img src={profile} alt="" />
//         </ProfileButton>
//       </HeaderWrapper>
      
//       {isSideBarOpen && (
//         <SideBar 
//           isOpen={isSideBarOpen} 
//           onClose={handleSideBarClose} 
//         />
//       )}
//     </>
//   );
// });

// Header.displayName = 'Header';

// export default Header;