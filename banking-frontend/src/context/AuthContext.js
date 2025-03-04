import React, { createContext, useState, useContext, useEffect } from 'react';
import api from '../services/api';
import { setAuthToken, clearAuthToken } from '../services/api';

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
      // In a real app, you'd verify the token here
      const userData = JSON.parse(localStorage.getItem('user') || '{}');
      setUser(userData);
      setIsAuthenticated(true);
    }
    setLoading(false);
  }, []);

  const login = async (username, password) => {
    setError(null);
    try {
      const response = await api.post('/banking/v1/auth/login', { username, password });
      const { token, user } = response.data; // Adjust based on your API response
      
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
      setAuthToken(token);
      setUser(user);
      setIsAuthenticated(true);
      return true;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to login');
      return false;
    }
  };

  const register = async (username, password, email) => {
    setError(null);
    try {
      // TODO: handle registration errors
      const response = await api.post('/banking/v1/auth/register', { 
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
    localStorage.removeItem('user');
    clearAuthToken();
    setUser(null);
    setIsAuthenticated(false);
  };

  return (
    <AuthContext.Provider value={{ 
      user, 
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
