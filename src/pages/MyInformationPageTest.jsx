// // src/pages/MyInformationPage.jsx
// import React from 'react';
// import { useNavigate } from 'react-router-dom';
// import styled from 'styled-components';
// import Header from '../components/Header';
// import Footer from '../components/Footer';

// const Container = styled.div`
//   display: flex;
//   justify-content: center;
//   align-items: center;
//   min-height: calc(100vh - 118px);
//   padding: 30px;
//   box-sizing: border-box;

//   @media (max-width: 768px) {
//     padding: 20px 10px;
//   }
// `;

// const ProfileCard = styled.div`
//   width: 900px;
//   height: 681px;
//   background: linear-gradient(135deg, #f8e8f0 0%, #e8d5e8 100%);
//   border-radius: 24px;
//   padding: 45px;
//   box-sizing: border-box;
//   font-family: '온글잎 의연체', sans-serif;
//   position: relative;
//   box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);

//   @media (max-width: 1200px) {
//     width: 90%;
//     max-width: 1100px;
//     height: auto;
//     min-height: 780px;
//   }

//   @media (max-width: 768px) {
//     width: 100%;
//     padding: 40px 30px;
//   }
// `;

// const ProfileHeader = styled.div`
//   text-align: center;
//   margin-bottom: 5px;
// `;

// const ProfileAvatar = styled.div`
//   margin-bottom: 10px;
// `;

// const AvatarCircle = styled.div`
//   width: 80px;
//   height: 80px;
//   border-radius: 50%;
//   background: white;
//   border: 4px solid #333;
//   display: flex;
//   align-items: center;
//   justify-content: center;
//   margin: 0 auto;
//   box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);

//   @media (max-width: 768px) {
//     width: 100px;
//     height: 100px;
//   }
// `;

// const AvatarEmoji = styled.span`
//   font-size: 36px;

//   @media (max-width: 768px) {
//     font-size: 36px;
//   }
// `;

// const Greeting = styled.h1`
//   font-size: 64px;
//   color: #333;
//   font-weight: bold;
//   margin: 0;
//   letter-spacing: 1px;

//   @media (max-width: 768px) {
//     font-size: 28px;
//   }
// `;

// const ProfileInfo = styled.div`
//   display: flex;
//   flex-direction: column;
//   gap: 20px;
//   margin-bottom: 60px;
// `;

// const InfoItem = styled.div`
//   background: white;
//   border-radius: 12px;
//   padding: 15px 25px;
//   box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
//   border: 1px solid rgba(0, 0, 0, 0.1);
// `;

// const InfoText = styled.span`
//   font-size: 40px;
//   color: #333;
//   font-weight: 500;
//   letter-spacing: 0.5px;
//   display: block;
//   text-align: center;

//   @media (max-width: 768px) {
//     font-size: 22px;
//   }
// `;

// const ProfileFooter = styled.div`
//   position: absolute;
//   bottom: 30px;
//   right: 45px;
// `;

// const EditButton = styled.button`
//   background: #ffffff;
//   border: 2px solid #ccc;
//   border-radius: 12px;
//   padding: 3px 30px;
//   font-size: 32px;
//   color: #363434;
//   cursor: pointer;
//   font-family: '온글잎 의연체', sans-serif;
//   font-weight: 500;
//   transition: all 0.2s ease;

//   &:hover {
//     background: #e8e8e8;
//     border-color: #999;
//   }

//   &:active {
//     transform: translateY(1px);
//   }
// `;

// const MyInformationPage = () => {
//   const navigate = useNavigate();

//   const handleEditClick = () => {
//     console.log('수정 버튼 클릭됨');
//     // navigate('/edit-profile');
//   };

//   return (
//     <>
//       <Container>
//         <ProfileCard>
//           <ProfileHeader>
//             <ProfileAvatar>
//               <AvatarCircle>
//                 <AvatarEmoji>🙂</AvatarEmoji>
//               </AvatarCircle>
//             </ProfileAvatar>
//             <Greeting>안녕하세요 양하진님</Greeting>
//           </ProfileHeader>

//           <ProfileInfo>
//             <InfoItem>
//               <InfoText>양하진 [ 닉네임 : 양상보 ]</InfoText>
//             </InfoItem>
//             <InfoItem>
//               <InfoText>생년월일 : 2002.03.12</InfoText>
//             </InfoItem>
//             <InfoItem>
//               <InfoText>
//                 성별 : 여
//                 <span style={{ marginLeft: '80px' }}>MBTI : ISTP</span>
//               </InfoText>
//             </InfoItem>
//             <InfoItem>
//               <InfoText>가입한 날 : 2025.06.25 (일기 쓴지 7일 째)</InfoText>
//             </InfoItem>
//           </ProfileInfo>

//           <ProfileFooter>
//             <EditButton onClick={handleEditClick}>수정</EditButton>
//           </ProfileFooter>
//         </ProfileCard>
//       </Container>
//     </>
//   );
// };

// export default MyInformationPage;
