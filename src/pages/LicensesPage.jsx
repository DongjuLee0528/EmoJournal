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

const Item = styled.div`
  line-height: 1.7;
  font-family: '온글잎 의연체', sans-serif;
  font-size: 18px;
`;

const Link = styled.a`
  color: #5c7cfa;
  text-decoration: underline;
`;

const LicensesPage = () => {
  return (
    <PageContainer>
      <Title>오픈소스 라이선스</Title>
      <Section>
        <Item>
          • React — MIT License (<Link href="https://github.com/facebook/react" target="_blank" rel="noreferrer">repository</Link>)
        </Item>
        <Item>
          • React Router — MIT License (<Link href="https://github.com/remix-run/react-router" target="_blank" rel="noreferrer">repository</Link>)
        </Item>
        <Item>
          • Styled Components — MIT License (<Link href="https://github.com/styled-components/styled-components" target="_blank" rel="noreferrer">repository</Link>)
        </Item>
      </Section>
    </PageContainer>
  );
};

export default LicensesPage;


