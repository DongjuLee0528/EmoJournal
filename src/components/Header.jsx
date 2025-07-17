import React from 'react';
import styled from 'styled-components';
import menu from '../image/menu.png';
import profile from '../image/profile.svg';
import { useNavigate } from 'react-router-dom';

// Styled-components 정의
const HeaderWrapper = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  background-color: #ffffff;
  border-bottom: 1px solid #e0e0e0;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;

  @media (max-width: 768px) {
    padding: 0 15px;
    height: 60px;
  }

  @media (max-width: 480px) {
    padding: 0 12px;
    height: 55px;
  }

  @media (max-width: 320px) {
    padding: 0 10px;
    height: 50px;
  }
`;

const MenuBar = styled.div`
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: #f5f5f5;
  }

  img {
    width: 24px;
    height: 24px;
    object-fit: contain;

    @media (max-width: 768px) {
      width: 22px;
      height: 22px;
    }

    @media (max-width: 480px) {
      width: 20px;
      height: 20px;
    }

    @media (max-width: 320px) {
      width: 18px;
      height: 18px;
    }
  }
`;

const Logo = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: transform 0.2s ease;

  &:hover {
    transform: scale(1.02);
  }
`;

const MainText = styled.p`
  font-family: 'Cherry Bomb One', cursive;
  color: rgba(255, 86, 139, 0.67);
  text-shadow: 0px 2px 4px rgba(0, 0, 0, 0.25);
  -webkit-text-stroke-width: 1px;
  -webkit-text-stroke-color: #060606;
  font-size: clamp(20px, 4vw, 36px);
  margin: 0;
  padding: 0;
  line-height: 1;
  white-space: nowrap;
`;

const SubText = styled.p`
  font-family: 'Cherry Bomb One', cursive;
  color: rgba(0, 0, 0, 0.83);
  font-size: clamp(10px, 2vw, 13px);
  margin: -2px 0 0 0;
  padding: 0;
  line-height: 1;
  white-space: nowrap;
`;

const ProfileBar = styled.div`
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: #f5f5f5;
  }

  img {
    width: 32px;
    height: 32px;
    object-fit: contain;

    @media (max-width: 768px) {
      width: 28px;
      height: 28px;
    }

    @media (max-width: 480px) {
      width: 26px;
      height: 26px;
    }

    @media (max-width: 320px) {
      width: 24px;
      height: 24px;
    }
  }
`;

// 컴포넌트
const Header = () => {
  const navigate = useNavigate();
  return (
    <HeaderWrapper>
      <MenuBar>
        <img src={menu} alt="메뉴바" />
      </MenuBar>
      <Logo onClick={() => navigate('/')}>
        <MainText>EMOJOURNAL</MainText>
        <SubText>My Mood Diary</SubText>
      </Logo>
      <ProfileBar>
        <img src={profile} alt="프로필바" />
      </ProfileBar>
    </HeaderWrapper>
  );
};

export default Header;
