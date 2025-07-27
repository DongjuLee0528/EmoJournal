// src/pages/MovePage.jsx
import React from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';


const Container = styled.div`
  text-align: center;
  height: 90vh;
`;

const Title = styled.h1`
  font-size: 2rem;
  margin-bottom: 2rem;
`;

const ButtonGroup = styled.div`
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 300px;
  margin: 0 auto;
`;

const NavButton = styled.button`
  padding: 1rem;
  font-size: 1rem;
  background-color: #4a90e2;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;

  &:hover {
    background-color: #357ab7;
  }
`;

const MovePage = () => {
  const navigate = useNavigate();

  return (
    <>
      <Container>
        <Title>이동 페이지</Title>
        <ButtonGroup>
          <NavButton onClick={() => navigate('/MainPage')}>메인 페이지</NavButton>
          <NavButton onClick={() => navigate('/DiaryListPage')}>일기 목록 페이지</NavButton>
          <NavButton onClick={() => navigate('/DiaryWritingPage')}>일기 작성 페이지</NavButton>
          <NavButton onClick={() => navigate('/LoginPageOauth')}>로그인 페이지Oauth</NavButton>
          <NavButton onClick={() => navigate('/MyInformationPage')}>내 정보 페이지</NavButton>
          <NavButton onClick={() => navigate('/MyInformationPageTest')}>내 정보 페이지 테스트용</NavButton>
        </ButtonGroup>
      </Container>
    </>
  );
};

export default MovePage;
