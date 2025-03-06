import React, { createContext, useState, useContext, useEffect } from 'react';
import api from '../services/api';
import { setAuthToken, clearAuthToken } from '../services/api';

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [username, setUsername] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      // TODO: Verify the token here
      setAuthToken(token);
      setUsername(localStorage.getItem('username'));
      setIsAuthenticated(true);
    }
    setLoading(false);
  }, []);

  const login = async (username, password) => {
    setError(null);
    try {
      const response = await api.post('/banking/v1/auth/login', { username, password });
      const { token } = response.data;
      
      localStorage.setItem('token', token);
      localStorage.setItem('username', username);
      setAuthToken(token);
      setUsername(username);
      setIsAuthenticated(true);
      return true;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to login');
      return false;
    }
  };

  const register = async (username, password, email) => {
    // TODO: automatically login after registration?
    setError(null);
    try {
      // TODO: handle registration errors
      await api.post('/banking/v1/auth/register', { 
        username, 
        password,
        email 
      });
      return true;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to register');
      return false;
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    clearAuthToken();
    setUsername(null);
    setIsAuthenticated(false);
  };

  return (
    <AuthContext.Provider value={{ 
      username, 
      isAuthenticated, 
      loading, 
      error,
      login, 
      register, 
      logout 
    }}>
      {children}
    </AuthContext.Provider>
  );
};