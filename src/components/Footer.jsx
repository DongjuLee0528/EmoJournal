import React from 'react';
import styled from 'styled-components';

const FooterWrapper = styled.div`
  width: 100%;
  background-color: #000000;
  padding: 4px 20px;
  text-align: center;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;

  p {
    color: #ffffff;
    font-size: 10px;
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
      font-size: 9px;
    }
  }

  /* 모바일 반응형 */
  @media (max-width: 480px) {
    padding: 3px 12px;

    p {
      font-size: 8px;
      line-height: 1.5;
    }
  }

  /* 초소형 모바일 */
  @media (max-width: 320px) {
    padding: 3px 10px;

    p {
      font-size: 7px;
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
