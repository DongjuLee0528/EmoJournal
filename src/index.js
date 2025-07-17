import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import GlobalStyle from './styles/GlobalStyle'; // ✅ styled-components 글로벌 스타일 추가
import reportWebVitals from './reportWebVitals';

// index.css는 생략하거나 초기화만 쓰는 용도
// import './index.css';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <GlobalStyle /> {/* ✅ 전체에 글로벌 스타일 적용 */}
    <App />
  </React.StrictMode>
);

reportWebVitals();
