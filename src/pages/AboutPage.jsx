import React from 'react';
import styled, { keyframes } from 'styled-components';
import profileSvg from '../image/profile.svg';

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
  
  @media (prefers-reduced-motion: reduce) {
    animation: none;
  }
  
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
  
  @media (prefers-reduced-motion: reduce) {
    animation: none;
  }
  
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
  line-height: 1.9;
  color: #555;
  text-align: center;
  margin-bottom: 16px;
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  @media (max-width: 768px) {
    font-size: 16px;
    line-height: 1.75;
    margin-bottom: 12px;
    max-width: 92%;
  }
  
  @media (max-width: 480px) {
    font-size: 15px;
    line-height: 1.7;
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
  
  @media (prefers-reduced-motion: reduce) {
    animation: none;
    transition: none;
    &:hover {
      transform: none;
      box-shadow: none;
    }
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
  font-size: 26px;
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

const CTAGroup = styled.div`
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 16px;
  flex-wrap: wrap;
`;

const CTAButton = styled.a`
  display: inline-block;
  padding: 12px 18px;
  border-radius: 999px;
  background: #ff6b6b;
  color: white;
  text-decoration: none;
  font-family: '온글잎 의연체', sans-serif;
  font-size: 18px;
  box-shadow: 0 4px 16px rgba(255, 107, 107, 0.3);
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(255, 107, 107, 0.4);
  }
  
  @media (prefers-reduced-motion: reduce) {
    transition: none;
    &:hover { transform: none; box-shadow: 0 4px 16px rgba(255, 107, 107, 0.3); }
  }
`;

const LinkButton = styled(CTAButton)`
  background: #845ef7;
  box-shadow: 0 4px 16px rgba(132, 94, 247, 0.25);
`;

const Avatar = styled.img`
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 14px;
  border: 2px solid rgba(0,0,0,0.06);
`;

const MemberLinks = styled.div`
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 12px;
`;

const MemberLink = styled.a`
  font-family: '온글잎 의연체', sans-serif;
  font-size: 14px;
  color: inherit;
  opacity: 0.8;
  text-decoration: underline;
`;

const Timeline = styled.ul`
  list-style: none;
  margin: 0 auto;
  padding: 0;
  max-width: 800px;
  text-align: center;
`;

const TimelineItem = styled.li`
  display: grid;
  grid-template-columns: 1fr;
  gap: 6px;
  align-items: center;
  justify-items: center;
  padding: 16px 0;
  border-bottom: 1px dashed rgba(0,0,0,0.08);

  &:last-child {
    border-bottom: none;
  }
`;

const TimelineDate = styled.div`
  font-family: '온글잎 의연체', sans-serif;
  font-size: 20px;
  color: #999;
  text-align: center;
`;

const TimelineText = styled.div`
  font-family: '온글잎 의연체', sans-serif;
  font-size: 22px;
  color: #444;
  line-height: 1.7;
  text-align: center;
  max-width: 700px;
`;

const LegalLinks = styled.div`
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
  margin-top: 8px;
`;

const LegalLink = styled.a`
  font-family: '온글잎 의연체', sans-serif;
  font-size: 18px;
  color: #555;
  text-decoration: underline;
`;

const AboutPage = () => {
  const teamMembers = [
    {
      id: 'ldj',
      name: "이동주",
      role: "Backend Developer",
      description: "백엔드 담당자 입니다..",
      avatar: profileSvg,
      github: 'https://github.com/DongjuLee0528'
    },
    {
      id: 'jhh',
      name: "정하형",
      role: "Backend Developer", 
      description: "백엔드 담당자 입니다.",
      avatar: profileSvg,
      github: 'https://github.com/justsicklife'
    },
    {
      id: 'ljh',
      name: "이지훈",
      role: "Frontend Developer",
      description: "프론트 담당자 입니다.",
      avatar: profileSvg,
      github: 'https://github.com/Neafrun'
    },
    {
      id: 'yhj',
      name: "양하진",
      role: "Frontend Developer",
      description: "프론트 담당자 입니다.",
      avatar: profileSvg,
      github: 'https://github.com/HJ0312'
    },
    {
      id: 'jwy',
      name: "장원영",
      role: "Frontend Developer",
      description: "프론트 담당자 입니다.",
      avatar: profileSvg,
      github: 'https://github.com/wonyoung10'
    }
  ];

  const jsonLd = {
    "@context": "https://schema.org",
    "@type": "AboutPage",
    "name": "EmoJournal에 대하여",
    "url": typeof window !== 'undefined' ? window.location.href : 'https://emojournal.example/about',
    "mainEntity": {
      "@type": "Organization",
      "name": "EmoJournal",
      "url": typeof window !== 'undefined' ? window.location.origin : 'https://emojournal.example',
      "sameAs": []
    }
  };

  return (
    <PageContainer>
      <Container>
        <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }} />
        <Title>EmoJournal에 대하여</Title>
        
        <Section delay={0.2}>
          <SectionTitle>🌟 우리팀의 한마디</SectionTitle>
          <Content>감정은 기록이 아닌, 당신의 이야기입니다.</Content>
          <Content>오늘 하루, 어떤 감정으로 빛났나요?</Content>
          <Content>'감정 일기장' 은 당신의 이야기를 담는 공간입니다.</Content>
          <Content>솔직한 마음을 기록하면, AI가 당신의 감정을 분석해 마음속 깊은 이야기를 들려줄 거예요.</Content>
          <Content>나도 몰랐던 내 마음과 마주하는 시간.</Content>
          <Content>진정한 나를 이해하는 가장 쉬운 방법, 여기서 당신의 이야기를 시작하세요.</Content>
          
        </Section>

        <Section delay={0.9}>
          <SectionTitle>🗓️ 로드맵 & 연혁</SectionTitle>
          <Timeline>
            <TimelineItem>
              <TimelineDate>2024.06</TimelineDate>
              <TimelineText>EmoJournal 아이디어 발굴 및 프로토타입 설계</TimelineText>
            </TimelineItem>
            <TimelineItem>
              <TimelineDate>2024.09</TimelineDate>
              <TimelineText>감정 분석 모델 PoC 및 기본 일기 작성 기능 완성</TimelineText>
            </TimelineItem>
            <TimelineItem>
              <TimelineDate>2024.12</TimelineDate>
              <TimelineText>베타 공개, 사용자 피드백 수집 및 UI/UX 개선</TimelineText>
            </TimelineItem>
            <TimelineItem>
              <TimelineDate>2025.03</TimelineDate>
              <TimelineText>감정 패턴 리포트, 주간/월간 통계 기능 출시</TimelineText>
            </TimelineItem>
          </Timeline>
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
                <BackendMember key={member.id} delay={0.8 + index * 0.1}>
                  <Avatar src={member.avatar} alt={`${member.name} 프로필`} />
                  <MemberName>{member.name}</MemberName>
                  <MemberRole>{member.role}</MemberRole>
                  <MemberDescription>{member.description}</MemberDescription>
                  {member.github && (
                    <MemberLinks>
                      <MemberLink href={member.github} target="_blank" rel="noreferrer">GitHub</MemberLink>
                    </MemberLinks>
                  )}
                </BackendMember>
              ))}
            </BackendGrid>
          </TeamCategory>

          <TeamCategory>
            <TeamCategoryTitle>🎨 Frontend Developers</TeamCategoryTitle>
            <FrontendGrid>
              {teamMembers.slice(2).map((member, index) => (
                <FrontendMember key={member.id} delay={1.0 + index * 0.1}>
                  <Avatar src={member.avatar} alt={`${member.name} 프로필`} />
                  <MemberName>{member.name}</MemberName>
                  <MemberRole>{member.role}</MemberRole>
                  <MemberDescription>{member.description}</MemberDescription>
                  {member.github && (
                    <MemberLinks>
                      <MemberLink href={member.github} target="_blank" rel="noreferrer">GitHub</MemberLink>
                    </MemberLinks>
                  )}
                </FrontendMember>
              ))}
            </FrontendGrid>
          </TeamCategory>
        </Section>

        <Section delay={1.2}>
          <SectionTitle>📄 저작권 정보</SectionTitle>
          <CopyrightSection>
            <CopyrightText>
              <strong>© 2024 EmoJournal Team. All rights reserved.</strong>
            </CopyrightText>
            <CopyrightText>
              본 웹사이트의 모든 콘텐츠(텍스트, 이미지, 디자인 등)는 EmoJournal 팀의 저작물입니다.
            </CopyrightText>
            <CopyrightText>
              • 온글잎 의연체 폰트는 온글잎의 저작물입니다.
            </CopyrightText>
            <LegalLinks>
              <LegalLink href="/terms">이용약관</LegalLink>
              <LegalLink href="/privacy">개인정보처리방침</LegalLink>
              <LegalLink href="/licenses">오픈소스 라이선스</LegalLink>
            </LegalLinks>
          </CopyrightSection>
        </Section>
      </Container>
    </PageContainer>
  );
};

export default AboutPage;