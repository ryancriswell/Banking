import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { CssBaseline, ThemeProvider, createTheme } from '@mui/material';
import config from './config/config';

// Check if we need to redirect to SSR app before rendering
const path = window.location.pathname;
if (path.startsWith('/ssr')) {
  const ssrPath = path.substring(path.indexOf('/ssr') + 4);
  window.location.href = `${config.ssrUrl}${ssrPath}${window.location.search}`;
} else {
  const theme = createTheme({
    palette: {
      primary: {
        main: '#1976d2',
      }
    },
  });

  const root = ReactDOM.createRoot(document.getElementById('root'));
  root.render(
    <React.StrictMode>
      <BrowserRouter basename='/spa'>
        <ThemeProvider theme={theme}>
          <CssBaseline />
          <AuthProvider>
            <App />
          </AuthProvider>
        </ThemeProvider>
      </BrowserRouter>
    </React.StrictMode>
  );
}
