import React from 'react';
import { Alert, Snackbar } from '@mui/material';

const ErrorAlert = ({ error, onClose }) => {
  return (
    <Snackbar 
      open={!!error} 
      autoHideDuration={6000} 
      onClose={onClose}
      anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
    >
      <Alert 
        onClose={onClose} 
        severity="error" 
        variant="filled"
      >
        {error}
      </Alert>
    </Snackbar>
  );
};

export default ErrorAlert;
