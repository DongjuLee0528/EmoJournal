import { createBrowserRouter, Outlet } from 'react-router-dom';
import MainPage from '../pages/MainPage';
import DiaryListPage from'../pages/DiaryListPage';
import DiaryWritingPage from '../pages/DiaryWritingPage';
import LoginPage from '../pages/LoginPage';
import MyInformationPage from '../pages/MyInformationPage';
import MovePage from '../pages/MovePage';
const router = createBrowserRouter([
    
  {
    children: [
      {
        path: '',
        element: <MovePage />,
      },
      {
        path: '/MainPage',
        element: <MainPage />,
      },
      {
        path: '/DiaryListPage',
        element: <DiaryListPage />,
      },
      {
        path: '/DiaryWritingPage',
        element: <DiaryWritingPage />,
      },
      {
        path: '/LoginPage',
        element: <LoginPage />,
      },
      {
        path: '/MyInformationPage',
        element: <MyInformationPage />,
      }
    ],
  },
]);

export default router;
