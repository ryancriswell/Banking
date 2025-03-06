import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import Layout from './components/Layout';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import TransactionHistory from './pages/TransactionHistory';
import Transfer from './pages/Transfer';
import LandingPage from './pages/LandingPage';

function App() {
  const { isAuthenticated } = useAuth();

  return (
    <Routes>
      {/* Root landing page to choose between SPA and SSR */}
      <Route path="/landing" element={<LandingPage />} />

      {/* SPA Routes */}
      <Route path="/login" element={!isAuthenticated ? <Login /> : <Navigate to="/dashboard" />} />
      <Route path="/register" element={!isAuthenticated ? <Register /> : <Navigate to="/dashboard" />} />
      
      {/* Protected SPA Routes */}
      <Route path="/" element={<Layout />}>
        <Route 
          path="dashboard" 
          element={isAuthenticated ? <Dashboard /> : <Navigate to="/login" />} 
        />
        <Route 
          path="transactions" 
          element={isAuthenticated ? <TransactionHistory /> : <Navigate to="/login" />} 
        />
        <Route 
          path="transfer" 
          element={isAuthenticated ? <Transfer /> : <Navigate to="/login" />} 
        />
      </Route>
      
      {/* Default redirect for SPA */}
      <Route path="/" element={<Navigate to={isAuthenticated ? "/dashboard" : "/login"} />} />
    </Routes>
  );
}

export default App;
