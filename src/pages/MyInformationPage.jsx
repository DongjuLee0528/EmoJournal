// src/pages/MyInformationPage.jsx
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import Header from '../components/Header';
import Footer from '../components/Footer';
import api from '../api/axiosInstance';

const Container = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 118px);
  font-size: 30px;
  font-family: '온글잎 의연체', sans-serif;
  padding: 30px;
  box-sizing: border-box;
  margin-top: 60px; /* Header 높이만큼 마진 추가 */

  @media (max-width: 768px) {
    padding: 20px 10px;
    margin-top: 60px;
  }

  @media (max-width: 480px) {
    margin-top: 55px;
  }

  @media (max-width: 320px) {
    margin-top: 50px;
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
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);

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

const AvatarImage = styled.img`
  width: 100%;
  height: 100%;
  object-fit: cover;
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

const LoadingMessage = styled.div`
  text-align: center;
  font-size: 24px;
  color: #666;
  padding: 40px;
`;

const MyInformationPage = () => {
  const navigate = useNavigate();
  const [member, setMember] = useState(null);
  const [nickname, setNickname] = useState('');
  const [gender, setGender] = useState('');
  const [mbti, setMbti] = useState('');
  const [originalData, setOriginalData] = useState({});
  const [isLoading, setIsLoading] = useState(false);

  const isMemberChanged = () => {
    return (
      member &&
      (member.nickname !== nickname || 
       member.gender !== gender ||
       member.mbti !== mbti) &&
      nickname.trim() !== "" && 
      gender !== "" && 
      mbti !== ""
    );
  };

  const getMemberInfo = async () => {
    try {
      setIsLoading(true);
      if(localStorage.getItem("accessToken")) {
        const res = await api.get("/member");
        console.log("회원 정보: ", res.data);

        const cleanedData = { ...res.data };
        for (let key in cleanedData) {
          if (cleanedData[key] === null) {
            cleanedData[key] = "";
          }
        }

        setMember(cleanedData);
        setNickname(cleanedData.nickname || '');
        setGender(cleanedData.gender || '');
        setMbti(cleanedData.mbti || '');
        
        // 원본 데이터 저장 (취소할 때 사용)
        setOriginalData({
          nickname: cleanedData.nickname || '',
          gender: cleanedData.gender || '',
          mbti: cleanedData.mbti || ''
        });
      }
    } catch(err) {
      console.error("회원 정보 조회 실패: ", err);
      alert("회원 정보를 불러오는데 실패했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    getMemberInfo();
  }, []);

  const handleSaveClick = async () => {
    if (!isMemberChanged()) return;

    try {
      setIsLoading(true);
      const res = await api.patch("/member", {
        nickname: nickname.trim(),
        gender,
        mbti
      });
      
      console.log("수정 완료:", res.data);
      
      // 성공시 member 상태 업데이트
      setMember(prev => ({
        ...prev,
        nickname: nickname.trim(),
        gender,
        mbti
      }));

      // 원본 데이터도 업데이트
      setOriginalData({
        nickname: nickname.trim(),
        gender,
        mbti
      });

      alert("정보가 성공적으로 수정되었습니다!");
      
    } catch(err) {
      console.error("정보 수정 실패:", err);
      alert("정보 수정에 실패했습니다. 다시 시도해주세요.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleCancelClick = () => {
    // 원본 데이터로 되돌리기
    setNickname(originalData.nickname);
    setGender(originalData.gender);
    setMbti(originalData.mbti);
  };

  if (isLoading) {
    return (
      <>
        <Header />
        <Container>
          <LoadingMessage>
            {member ? "정보를 수정하는 중입니다..." : "사용자 정보를 불러오는 중입니다..."}
          </LoadingMessage>
        </Container>
        <Footer />
      </>
    );
  }

  if (!member) {
    return (
      <>
        <Header />
        <Container>
          <LoadingMessage>
            사용자 정보를 불러올 수 없습니다. 로그인이 필요하거나 오류가 발생했습니다.
          </LoadingMessage>
        </Container>
        <Footer />
      </>
    );
  }

  return (
    <>
      <Header />
      <Container>
        <ProfileCard>
          <ProfileHeader>
            <ProfileAvatar>
              <AvatarCircle>
                {member.picture ? (
                  <AvatarImage src={member.picture} alt="프로필 사진" />
                ) : (
                  <AvatarEmoji>🙂</AvatarEmoji>
                )}
              </AvatarCircle>
            </ProfileAvatar>
            <Greeting>안녕하세요 {nickname || '사용자'}님</Greeting>
          </ProfileHeader>

          <ProfileInfo>
            <InfoItem>
              <InfoText>
                <span>이메일:</span>
                <span>{member.email}</span>
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
                <span>{member.createDate?.split('T')[0]}</span>
              </InfoText>
            </InfoItem>
          </ProfileInfo>

          <ProfileFooter>
            {isMemberChanged() && (
              <CancelButton onClick={handleCancelClick}>
                취소
              </CancelButton>
            )}
            <SaveButton 
              disabled={!isMemberChanged() || isLoading}
              onClick={handleSaveClick}
            >
              {isLoading ? "저장중..." : "저장"}
            </SaveButton>
          </ProfileFooter>
        </ProfileCard>
      </Container>
      <Footer />
    </>
  );
};

export default MyInformationPage;