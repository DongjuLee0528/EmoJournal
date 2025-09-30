import { createGlobalStyle } from 'styled-components';
import bgImage from '../image/background.png'; // 이미지 경로

const GlobalStyle = createGlobalStyle`
  @import url('https://fonts.googleapis.com/css2?family=Cherry+Bomb+One&display=swap');
  * {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
  }

  body {
    font-family: 'Cherry Bomb One', cursive;
    background-image: url(${bgImage});
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center;
    min-height: 100vh;
  }
    main {
    padding-top: 60px; /* 헤더 높이와 동일하게 */
  }

  @media (max-width: 480px) {
    main {
      padding-top: 55px;
    }
  }

  @media (max-width: 320px) {
    main {
      padding-top: 50px;
    }
  }
`;

export default GlobalStyle;
