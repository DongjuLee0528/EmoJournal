// import React, { useState } from 'react';
// import { useNavigate } from 'react-router-dom';
// import styled from 'styled-components';

// // Styled Components (간략하게만 유지)
// const Container = styled.div`
//   min-height: 78vh;
//   display: flex;
//   align-items: center;
//   justify-content: center;
// `;

// const Card = styled.div`
//   background: rgba(255, 255, 255, 0.9);
//   backdrop-filter: blur(10px);
//   border-radius: 24px;
//   padding: 2rem;
//   box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
//   width: 100%;
//   max-width: 400px;
//   margin: 1rem;
// `;

// const Title = styled.h1`
//   font-size: 2.5rem;
//   font-weight: bold;
//   color: #374151;
//   text-align: center;
//   margin-bottom: 0.5rem;
// `;

// const Subtitle = styled.p`
//   color: #6b7280;
//   font-size: 1.4rem;
//   text-align: center;
//   margin-bottom: 2rem;
//   font-family: '온글잎 의연체', sans-serif;
// `;

// const Button = styled.button`
//   width: 100%;
//   background: #4285f4;
//   color: white;
//   font-weight: 600;
//   padding: 0.75rem 1rem;
//   border-radius: 50px;
//   border: none;
//   cursor: pointer;
//   font-size: 1rem;
//   transition: all 0.2s ease-in-out;

//   &:hover {
//     background: #357ae8;
//   }

//   &:disabled {
//     opacity: 0.6;
//     cursor: not-allowed;
//   }
// `;

// const Message = styled.div`
//   margin-top: 1rem;
//   font-size: 1.4rem;
//   text-align: center;
//   font-family: '온글잎 의연체', sans-serif;
//   color: ${({ error }) => (error ? '#dc2626' : '#065f46')};
// `;

// // 테스트용 사용자 데이터 (원하는대로 추가 가능)
// const TEST_USERS = [
//   { id: '1', name: '이지훈', email: 'ljh@test.com' },
//   { id: '2', name: '양하진', email: 'yhj@test.com' },
//   { id: '3', name: '이동주', email: 'ldj@test.com' },
// ];

// const LoginPageTest = () => {
//   const navigate = useNavigate();
//   const [isLoading, setIsLoading] = useState(false);
//   const [message, setMessage] = useState('');
//   const [isError, setIsError] = useState(false);

//   // 테스트 로그인 핸들러 (랜덤 사용자)
//   const handleTestLogin = () => {
//     setIsLoading(true);
//     setIsError(false);
//     setMessage('로그인 중... 잠시만 기다려주세요.');

//     setTimeout(() => {
//       // 랜덤 사용자 선택
//       const randomUser = TEST_USERS[Math.floor(Math.random() * TEST_USERS.length)];

//       // 로컬 스토리지에 임시 토큰과 사용자 정보 저장
//       localStorage.setItem('accessToken', `fake_token_${randomUser.id}_${Date.now()}`);
//       localStorage.setItem('user', JSON.stringify(randomUser));

//       setMessage(`${randomUser.name}님 환영합니다! 메인페이지로 이동합니다.`);
//       setIsLoading(false);

//       // 1초 후 메인 페이지로 이동
//       setTimeout(() => {
//         navigate('/MainPage');  // 네가 원하는 경로로 변경 가능
//       }, 1000);
//     }, 1500);
//   };

//   return (
//     <Container>
//       <Card>
//         <Title>Login Test Page</Title>
//         <Subtitle>Google 로그인 없이 테스트용 랜덤 로그인</Subtitle>
//         <Button onClick={handleTestLogin} disabled={isLoading}>
//           {isLoading ? '로그인 중...' : '테스트 로그인'}
//         </Button>
//         {message && <Message error={isError}>{message}</Message>}
//       </Card>
//     </Container>
//   );
// };

// export default LoginPageTest;
