import { createGlobalStyle } from 'styled-components';
import bgImage from '../image/background.png';
import OngleapFont from '../fonts/온글잎 의연체.ttf';

const GlobalStyle = createGlobalStyle`
  @import url('https://fonts.googleapis.com/css2?family=Cherry+Bomb+One&display=swap');

  @font-face {
    font-family: '온글잎 의연체';
    src: url('${OngleapFont}') format('truetype');
    font-display: block;
    font-weight: normal;
    font-style: normal;
  }

  * {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
  }

  body {
    font-family: '온글잎 의연체', 'Cherry Bomb One', cursive;
    background-image: url(${bgImage});
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center;
    min-height: 100vh;
    overflow-y: scroll;
    visibility: hidden; /* 폰트 로딩 중 숨김 */
  }

  body.font-loaded {
    visibility: visible; /* 폰트 로딩 완료 시 표시 */
  }

`;

export default GlobalStyle;
