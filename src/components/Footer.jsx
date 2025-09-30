import React from 'react';
import styled from 'styled-components';

const FooterWrapper = styled.div`
  width: 100%;
  /* [수정] 배경색을 부드럽게 변경 */
  background-color: #333;
  padding: 4px 20px;
  text-align: center;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  
  p {
    color: #ffffff;
    /* [수정] 가독성을 위해 기본 폰트 크기 조정 */
    font-size: 12px;
    margin: 0;
    padding: 0;
    line-height: 1.4;
    font-weight: normal;
    letter-spacing: 0.3px;
  }
  
  /* 태블릿 반응형 */
  @media (max-width: 768px) {
    padding: 4px 15px;
    
    p {
      /* [수정] 폰트 크기 조정 */
      font-size: 11px;
    }
  }
  
  /* 모바일 반응형 */
  @media (max-width: 480px) {
    padding: 3px 12px;
    
    p {
      /* [수정] 최소 폰트 크기를 10px로 유지 */
      font-size: 10px;
      line-height: 1.5;
    }
  }
  
  /* 초소형 모바일 */
  @media (max-width: 320px) {
    padding: 3px 10px;
    
    p {
      /* [수정] 최소 폰트 크기를 10px로 유지 */
      font-size: 10px;
      line-height: 1.6;
    }
  }
`;

const Footer = () => {
  return (
    <FooterWrapper>
      <p>© 2025 EmoJournal. All rights reserved. Another day, another feeling to remember.</p>
    </FooterWrapper>
  );
};

export default Footer;
