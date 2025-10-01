import React from 'react';
import styled from 'styled-components';

const PageContainer = styled.div`
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 20px 80px;
`;

const Title = styled.h1`
  font-family: '온글잎 의연체', sans-serif;
  font-size: 36px;
  text-align: center;
  margin-bottom: 24px;
`;

const Section = styled.section`
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  margin-bottom: 16px;
`;

const P = styled.p`
  line-height: 1.8;
  font-family: '온글잎 의연체', sans-serif;
  font-size: 18px;
`;

const TermsPage = () => {
  return (
    <PageContainer>
      <Title>이용약관</Title>
      <Section>
        <P>본 약관은 EmoJournal 서비스의 이용과 관련하여 기본적인 권리, 의무 및 책임 사항을 규정합니다.</P>
        <P>정식 문서가 준비되기 전까지는 이 페이지가 임시 안내문 역할을 합니다.</P>
      </Section>
    </PageContainer>
  );
};

export default TermsPage;


