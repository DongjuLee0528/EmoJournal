import React from 'react';
import styled, { keyframes } from 'styled-components';

const fadeInUp = keyframes`
  0% {
    opacity: 0;
    transform: translateY(20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
`;

const slideInLeft = keyframes`
  0% {
    opacity: 0;
    transform: translateX(-30px);
  }
  100% {
    opacity: 1;
    transform: translateX(0);
  }
`;

const PageContainer = styled.div`
  min-height: 100vh;
  background: linear-gradient(135deg, #ffeef8 0%, #f8f2ff 100%);
  padding: 40px 20px 40px;
  
  @media (max-width: 768px) {
    padding: 60px 15px 40px;
  }
`;

const Container = styled.div`
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
`;

const Title = styled.h1`
  font-family: '온글잎 의연체', sans-serif;
  font-size: 48px;
  text-align: center;
  color: #333;
  margin-bottom: 40px;
  animation: ${fadeInUp} 0.8s ease;
  
  @media (max-width: 768px) {
    font-size: 36px;
    margin-bottom: 30px;
  }
  
  @media (max-width: 480px) {
    font-size: 28px;
  }
`;

const Section = styled.div`
  background: white;
  border-radius: 20px;
  padding: 50px;
  margin-bottom: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  animation: ${fadeInUp} 0.8s ease ${props => props.delay || 0}s both;
  
  @media (max-width: 768px) {
    padding: 35px 30px;
  }
  
  @media (max-width: 480px) {
    padding: 30px 25px;
    margin-bottom: 25px;
  }
`;

const SectionTitle = styled.h2`
  font-family: '온글잎 의연체', sans-serif;
  font-size: 32px;
  color: #ff6b6b;
  margin-bottom: 25px;
  text-align: center;
  
  @media (max-width: 768px) {
    font-size: 28px;
    margin-bottom: 20px;
  }
  
  @media (max-width: 480px) {
    font-size: 24px;
  }
`;

const Content = styled.p`
  font-family: '온글잎 의연체', sans-serif;
  font-size: 26px;
  line-height: 1.8;
  color: #555;
  text-align: center;
  margin-bottom: 20px;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  @media (max-width: 768px) {
    font-size: 16px;
    line-height: 1.7;
  }
  
  @media (max-width: 480px) {
    font-size: 15px;
  }
`;

const TeamGrid = styled.div`
  display: grid;
  gap: 20px;
  margin-top: 30px;
`;

const BackendGrid = styled(TeamGrid)`
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  
  @media (max-width: 480px) {
    grid-template-columns: 1fr;
  }
`;

const FrontendGrid = styled(TeamGrid)`
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  
  @media (max-width: 480px) {
    grid-template-columns: 1fr;
  }
`;

const TeamCategory = styled.div`
  margin-bottom: 30px;
  
  &:last-child {
    margin-bottom: 0;
  }
`;

const TeamCategoryTitle = styled.h3`
  font-family: '온글잎 의연체', sans-serif;
  font-size: 28px;
  color: #666;
  margin-bottom: 20px;
  text-align: center;
  
  @media (max-width: 480px) {
    font-size: 20px;
  }
`;

const TeamMember = styled.div`
  border-radius: 15px;
  padding: 30px;
  text-align: center;
  animation: ${slideInLeft} 0.6s ease ${props => props.delay || 0}s both;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  }
  
  @media (max-width: 480px) {
    padding: 25px;
  }
`;

const BackendMember = styled(TeamMember)`
  background: #eff6ff;
  border: 1px solid #dbeafe;
  color: #1e40af;
  
  &:hover {
    background: #dbeafe;
  }
`;

const FrontendMember = styled(TeamMember)`
  background: #fdf2f8;
  border: 1px solid #fce7f3;
  color: #be185d;
  
  &:hover {
    background: #fce7f3;
  }
`;

const MemberName = styled.h4`
  font-family: '온글잎 의연체', sans-serif;
  font-size: 30px;
  margin-bottom: 10px;
  font-weight: bold;
  
  @media (max-width: 480px) {
    font-size: 20px;
  }
`;

const MemberRole = styled.p`
  font-family: '온글잎 의연체', sans-serif;
  font-size: 30px;
  opacity: 0.8;
  margin: 5px 0 15px 0;
  font-weight: 500;
  
  @media (max-width: 480px) {
    font-size: 14px;
  }
`;

const MemberDescription = styled.p`
  font-family: '온글잎 의연체', sans-serif;
  font-size: 23px;
  opacity: 0.9;
  line-height: 1.5;
  color: #666;
  
  @media (max-width: 480px) {
    font-size: 13px;
  }
`;

const CopyrightSection = styled.div`
  background: #f8f9fa;
  border-radius: 15px;
  padding: 25px;
  margin-top: 20px;
  border-left: 5px solid #ff6b6b;
  
  @media (max-width: 480px) {
    padding: 20px;
  }
`;

const CopyrightText = styled.p`
  font-family: '온글잎 의연체', sans-serif;
  font-size: 25px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 10px;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  @media (max-width: 480px) {
    font-size: 13px;
  }
`;

const AboutPage = () => {
  const teamMembers = [
    {
      name: "이동주",
      role: "Backend Developer",
      description: "백엔드 담당자 입니다.."
    },
    {
      name: "정하형",
      role: "Backend Developer", 
      description: "백엔드 담당자 입니다."
    },
    {
      name: "이지훈",
      role: "Frontend Developer",
      description: "프론트 담당자 입니다."
    },
    {
      name: "양하진",
      role: "Frontend Developer",
      description: "프론트 담당자 입니다."
    },
    {
      name: "장원영",
      role: "Frontend Developer",
      description: "프론트 담당자 입니다."
    }
  ];

  return (
    <PageContainer>
      <Container>
        <Title>EmoJournal에 대하여</Title>
        
        <Section delay={0.2}>
          <SectionTitle>🌟 우리팀의 한마디</SectionTitle>
          <Content>
            "감정은 단순한 기록이 아닌, 우리 삶의 소중한 이야기입니다."
          </Content>
          <Content>
            매일의 감정을 기록하고 되돌아보며, 더 나은 내일을 만들어가는 여정에 함께하고 싶었습니다.
          </Content>
        </Section>

        <Section delay={0.4}>
          <SectionTitle>💝 만들게 된 이유</SectionTitle>
          <Content>
            현대인들은 바쁜 일상 속에서 자신의 감정을 돌아볼 시간이 부족합니다.
            특히 감정 표현이 어려운 사람들에게는 더욱 그렇죠.
          </Content>
          <Content>
            EmoJournal은 단순한 일기장이 아닌, 감정을 시각화하고 패턴을 분석하여
            자신을 더 잘 이해할 수 있도록 도와주는 감정 관리 도구입니다.
          </Content>
          <Content>
            우리는 모든 사람이 자신의 감정을 건강하게 표현하고 관리할 수 있기를 바랍니다.
          </Content>
        </Section>

        <Section delay={0.6}>
          <SectionTitle>👥 팀원 소개</SectionTitle>
          
          <TeamCategory>
            <TeamCategoryTitle>⚙️ Backend Developers</TeamCategoryTitle>
            <BackendGrid>
              {teamMembers.slice(0, 2).map((member, index) => (
                <BackendMember key={index} delay={0.8 + index * 0.1}>
                  <MemberName>{member.name}</MemberName>
                  <MemberRole>{member.role}</MemberRole>
                  <MemberDescription>{member.description}</MemberDescription>
                </BackendMember>
              ))}
            </BackendGrid>
          </TeamCategory>

          <TeamCategory>
            <TeamCategoryTitle>🎨 Frontend Developers</TeamCategoryTitle>
            <FrontendGrid>
              {teamMembers.slice(2).map((member, index) => (
                <FrontendMember key={index} delay={1.0 + index * 0.1}>
                  <MemberName>{member.name}</MemberName>
                  <MemberRole>{member.role}</MemberRole>
                  <MemberDescription>{member.description}</MemberDescription>
                </FrontendMember>
              ))}
            </FrontendGrid>
          </TeamCategory>
        </Section>

        <Section delay={1.4}>
          <SectionTitle>📄 저작권 정보</SectionTitle>
          <CopyrightSection>
            <CopyrightText>
              <strong>© 2024 EmoJournal Team. All rights reserved.</strong>
            </CopyrightText>
            <CopyrightText>
              본 웹사이트의 모든 콘텐츠(텍스트, 이미지, 디자인 등)는 EmoJournal 팀의 저작물입니다.
            </CopyrightText>
            <CopyrightText>
              사용된 오픈소스 라이브러리들은 각각의 라이선스를 따릅니다:
            </CopyrightText>
            <CopyrightText>
              • React (MIT License)
            </CopyrightText>
            <CopyrightText>
              • React Router (MIT License)
            </CopyrightText>
            <CopyrightText>
              • Styled Components (MIT License)
            </CopyrightText>
            <CopyrightText>
              • 온글잎 의연체 폰트는 온글잎의 저작물입니다.
            </CopyrightText>
            <CopyrightText style={{ marginTop: '15px', fontSize: '12px', opacity: 0.7 }}>
              문의사항이 있으시면 @emojournal.com으로 연락해 주세요.
            </CopyrightText>
          </CopyrightSection>
        </Section>
      </Container>
    </PageContainer>
  );
};

export default AboutPage;