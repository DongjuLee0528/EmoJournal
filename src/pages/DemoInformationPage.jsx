// src/pages/DemoInformationPage.jsx
import React, { useState } from 'react';
import styled from 'styled-components';
import Header from '../components/Header';
import Footer from '../components/Footer';

const Container = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 85vh;
  font-size: 30px;
  font-family: '온글잎 의연체', sans-serif;
  padding: 30px;
  box-sizing: border-box;

  @media (max-width: 768px) {
    padding: 20px 10px;
  }
`;

const ProfileCard = styled.div`
  width: 900px;
  min-height: 681px;
  background: linear-gradient(135deg, #f8e8f0 0%, #e8d5e8 100%);
  border-radius: 24px;
  padding: 45px;
  box-sizing: border-box;
  font-family: '온글잎 의연체', sans-serif;
  position: relative;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);

  @media (max-width: 1200px) {
    width: 90%;
    max-width: 1100px;
    height: auto;
    min-height: 780px;
  }

  @media (max-width: 768px) {
    width: 100%;
    padding: 40px 30px;
  }
`;

const DemoLabel = styled.div`
  position: absolute;
  top: -15px;
  left: 50%;
  transform: translateX(-50%);
  background: #ff6b6b;
  color: white;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 18px;
  font-weight: bold;
  box-shadow: 0 4px 16px rgba(255, 107, 107, 0.3);
  z-index: 10;

  @media (max-width: 768px) {
    font-size: 14px;
    padding: 6px 15px;
  }
`;

const ProfileHeader = styled.div`
  text-align: center;
  margin-bottom: 5px;
`;

const ProfileAvatar = styled.div`
  margin-bottom: 10px;
`;

const AvatarCircle = styled.div`
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: white;
  border: 4px solid #333;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  overflow: hidden;

  @media (max-width: 768px) {
    width: 100px;
    height: 100px;
  }
`;

const AvatarEmoji = styled.span`
  font-size: 36px;

  @media (max-width: 768px) {
    font-size: 36px;
  }
`;

const Greeting = styled.h1`
  font-size: 64px;
  color: #333;
  font-weight: bold;
  margin: 0;
  letter-spacing: 1px;

  @media (max-width: 768px) {
    font-size: 28px;
  }
`;

const ProfileInfo = styled.div`
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 60px;
`;

const InfoItem = styled.div`
  background: white;
  border-radius: 12px;
  padding: 15px 25px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.1);
`;

const InfoText = styled.div`
  font-size: 40px;
  color: #333;
  font-weight: 500;
  letter-spacing: 0.5px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;

  @media (max-width: 768px) {
    font-size: 22px;
    flex-direction: column;
    gap: 10px;
  }
`;

const InputField = styled.input`
  font-size: 32px;
  padding: 8px 15px;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-family: '온글잎 의연체', sans-serif;
  background: #f9f9f9;
  color: #333;
  min-width: 200px;
  transition: all 0.2s ease;

  &:focus {
    outline: none;
    border-color: #999;
    background: white;
  }

  @media (max-width: 768px) {
    font-size: 18px;
    min-width: 150px;
  }
`;

const SelectField = styled.select`
  font-size: 32px;
  padding: 8px 15px;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-family: '온글잎 의연체', sans-serif;
  background: #f9f9f9;
  color: #333;
  min-width: 150px;
  transition: all 0.2s ease;

  &:focus {
    outline: none;
    border-color: #999;
    background: white;
  }

  @media (max-width: 768px) {
    font-size: 18px;
    min-width: 120px;
  }
`;

const ProfileFooter = styled.div`
  position: absolute;
  bottom: 30px;
  right: 45px;
  display: flex;
  gap: 15px;
`;

const Button = styled.button`
  background: #ffffff;
  border: 2px solid #ccc;
  border-radius: 12px;
  padding: 8px 30px;
  font-size: 32px;
  color: #363434;
  cursor: pointer;
  font-family: '온글잎 의연체', sans-serif;
  font-weight: 500;
  transition: all 0.2s ease;

  &:hover {
    background: #e8e8e8;
    border-color: #999;
  }

  &:active {
    transform: translateY(1px);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
    background: #f5f5f5;
    border-color: #ddd;
  }

  @media (max-width: 768px) {
    font-size: 20px;
    padding: 6px 20px;
  }
`;

const SaveButton = styled(Button)`
  background: #4CAF50;
  color: white;
  border-color: #4CAF50;

  &:hover:not(:disabled) {
    background: #45a049;
    border-color: #45a049;
  }
`;

const CancelButton = styled(Button)`
  background: #f44336;
  color: white;
  border-color: #f44336;

  &:hover {
    background: #da190b;
    border-color: #da190b;
  }
`;

const ResetButton = styled(Button)`
  background: #2196F3;
  color: white;
  border-color: #2196F3;

  &:hover {
    background: #1976D2;
    border-color: #1976D2;
  }
`;

const DemoInformationPage = () => {
  // 데모용 초기 데이터
  const initialData = {
    email: "demo@example.com",
    nickname: "데모사용자",
    gender: "MALE",
    mbti: "ENFP",
    createDate: "2024-01-15",
    picture: null
  };

  const [nickname, setNickname] = useState(initialData.nickname);
  const [gender, setGender] = useState(initialData.gender);
  const [mbti, setMbti] = useState(initialData.mbti);
  const [originalData, setOriginalData] = useState({
    nickname: initialData.nickname,
    gender: initialData.gender,
    mbti: initialData.mbti
  });

  const isDataChanged = () => {
    return (
      originalData.nickname !== nickname || 
      originalData.gender !== gender ||
      originalData.mbti !== mbti
    ) && nickname.trim() !== "" && gender !== "" && mbti !== "";
  };

  const handleSaveClick = () => {
    if (!isDataChanged()) return;

    // 데모이므로 실제 API 호출 대신 상태만 업데이트
    setOriginalData({
      nickname: nickname.trim(),
      gender,
      mbti
    });

    alert("데모 버전입니다. 실제로는 서버에 저장됩니다!");
  };

  const handleCancelClick = () => {
    setNickname(originalData.nickname);
    setGender(originalData.gender);
    setMbti(originalData.mbti);
  };

  const handleResetClick = () => {
    setNickname(initialData.nickname);
    setGender(initialData.gender);
    setMbti(initialData.mbti);
    setOriginalData({
      nickname: initialData.nickname,
      gender: initialData.gender,
      mbti: initialData.mbti
    });
  };

  return (
    <>
    <Header />
    <Container>
      <ProfileCard>
        <ProfileHeader>
          <ProfileAvatar>
            <AvatarCircle>
              <AvatarEmoji></AvatarEmoji>
            </AvatarCircle>
          </ProfileAvatar>
          <Greeting>안녕하세요 {nickname}님</Greeting>
        </ProfileHeader>

        <ProfileInfo>
          <InfoItem>
            <InfoText>
              <span>이메일:</span>
              <span>{initialData.email}</span>
            </InfoText>
          </InfoItem>

          <InfoItem>
            <InfoText>
              <span>닉네임:</span>
              <InputField
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                placeholder="닉네임을 입력하세요"
                maxLength={20}
              />
            </InfoText>
          </InfoItem>

          <InfoItem>
            <InfoText>
              <span>성별:</span>
              <SelectField 
                value={gender} 
                onChange={(e) => setGender(e.target.value)}
              >
                <option value="">선택하세요</option>
                <option value="MALE">남성</option>
                <option value="FEMALE">여성</option>
              </SelectField>
            </InfoText>
          </InfoItem>

          <InfoItem>
            <InfoText>
              <span>MBTI:</span>
              <SelectField 
                value={mbti} 
                onChange={(e) => setMbti(e.target.value)}
              >
                <option value="">선택하세요</option>
                {["INTJ", "INTP", "ENTJ", "ENTP", "INFJ", "INFP", "ENFJ", "ENFP",
                  "ISTJ", "ISFJ", "ESTJ", "ESFJ", "ISTP", "ISFP", "ESTP", "ESFP"]
                  .map(type => (
                    <option key={type} value={type}>{type}</option>
                  ))}
              </SelectField>
            </InfoText>
          </InfoItem>

          <InfoItem>
            <InfoText>
              <span>가입한 날:</span>
              <span>{initialData.createDate}</span>
            </InfoText>
          </InfoItem>
        </ProfileInfo>

        <ProfileFooter>
          <ResetButton onClick={handleResetClick}>
            초기화
          </ResetButton>
          {isDataChanged() && (
            <CancelButton onClick={handleCancelClick}>
              취소
            </CancelButton>
          )}
          <SaveButton 
            disabled={!isDataChanged()}
            onClick={handleSaveClick}
          >
            저장
          </SaveButton>
        </ProfileFooter>
      </ProfileCard>
    </Container>
    <Footer />
    </>
  );
};

export default DemoInformationPage;