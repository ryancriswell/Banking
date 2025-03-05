import React, { useState, useEffect } from 'react';
import { 
  Typography, Paper, TextField, Button, Box, 
  CircularProgress, Alert, Stepper, Step, 
  StepLabel, Card, CardContent, Grid, Divider 
} from '@mui/material';
import { fetchBalance, transferFunds } from '../services/bankingService';
import ErrorAlert from '../components/ErrorAlert';

const steps = ['Enter Transfer Details', 'Confirm Transfer', 'Transfer Complete'];

const Transfer = () => {
  const [activeStep, setActiveStep] = useState(0);
  const [formData, setFormData] = useState({
    recipientUsername: '',
    amount: ''
  });
  const [balance, setBalance] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [formErrors, setFormErrors] = useState({
    recipientUsername: '',
    amount: ''
  });
  const [transferResult, setTransferResult] = useState(null);

  const loadBalance = async () => {
    try {
      const response = await fetchBalance();
      setBalance(response.balance);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load balance');
    }
  };
  
  useEffect(() => {
    loadBalance();
  }, []);

  const validateForm = () => {
    let valid = true;
    const errors = {
      recipientUsername: '',
      amount: ''
    };

    if (!formData.recipientUsername) {
      errors.recipientUsername = 'Recipient is required';
      valid = false;
    }

    if (!formData.amount) {
      errors.amount = 'Amount is required';
      valid = false;
    } else {
      const amount = parseFloat(formData.amount);
      if (isNaN(amount) || amount <= 0) {
        errors.amount = 'Amount must be a positive number';
        valid = false;
      } else if (balance !== null && amount > balance) {
        errors.amount = 'Insufficient funds';
        valid = false;
      }
    }

    setFormErrors(errors);
    return valid;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
    
    // Clear error when user types
    if (formErrors[name]) {
      setFormErrors({
        ...formErrors,
        [name]: ''
      });
    }
  };

  const handleNext = () => {
    if (activeStep === 0 && !validateForm()) {
      return;
    }

    if (activeStep === 1) {
      handleTransfer();
    } else {
      setActiveStep((prevStep) => prevStep + 1);
    }
  };

  const handleBack = () => {
    setActiveStep((prevStep) => prevStep - 1);
  };

  const handleTransfer = async () => {
    setLoading(true);
    setError(null);

    try {
      const amount = parseFloat(formData.amount);
      const result = await transferFunds(formData.recipientUsername, amount);
      setTransferResult(result);
      loadBalance(); // Refresh balance after transfer
      setActiveStep(2); // Move to completion step
    } catch (err) {
      setError(err.response?.data?.message || 'Transfer failed');
      setActiveStep(0); // Return to form on error
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setActiveStep(0);
    setFormData({
      recipientUsername: '',
      amount: ''
    });
    setTransferResult(null);
    setFormErrors({
      recipientUsername: '',
      amount: ''
    });
  };

  return (
    <>
      <Typography variant="h4" gutterBottom>
        Transfer Funds
      </Typography>
      
      <Paper sx={{ p: 3 }}>
        <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
          {steps.map((label) => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>
        
        {activeStep === 0 && (
          <>
            <Grid container spacing={2}>
              <Grid item xs={12} md={6}>
                <Card variant="outlined" sx={{ mb: 3 }}>
                  <CardContent>
                    <Typography variant="subtitle1" color="textSecondary" gutterBottom>
                      Available Balance
                    </Typography>
                    <Typography variant="h4">
                      ${balance !== null ? balance.toFixed(2) : '---'}
                    </Typography>
                  </CardContent>
                </Card>
                
                <Box component="form" noValidate>
                  <TextField
                    margin="normal"
                    required
                    fullWidth
                    id="recipientUsername"
                    label="Recipient Username"
                    name="recipientUsername"
                    autoComplete="off"
                    value={formData.recipientUsername}
                    onChange={handleChange}
                    error={!!formErrors.recipientUsername}
                    helperText={formErrors.recipientUsername}
                  />
                  <TextField
                    margin="normal"
                    required
                    fullWidth
                    id="amount"
                    label="Amount"
                    name="amount"
                    type="number"
                    inputProps={{ min: 0.01, step: 0.01 }}
                    value={formData.amount}
                    onChange={handleChange}
                    error={!!formErrors.amount}
                    helperText={formErrors.amount}
                  />
                </Box>
              </Grid>
              <Grid item xs={12} md={6}>
                <Alert severity="info" sx={{ mb: 2 }}>
                  Transfer funds to another user by entering their username and the amount.
                </Alert>
                <Typography variant="body2" color="textSecondary" paragraph>
                  • Transfers are processed immediately
                </Typography>
                <Typography variant="body2" color="textSecondary" paragraph>
                  • You cannot transfer more than your available balance
                </Typography>
                <Typography variant="body2" color="textSecondary" paragraph>
                  • Make sure the recipient username is correct
                </Typography>
              </Grid>
            </Grid>
          </>
        )}
        
        {activeStep === 1 && (
          <>
            <Alert severity="warning" sx={{ mb: 3 }}>
              Please review your transfer details before confirming.
            </Alert>
            
            <Grid container spacing={2}>
              <Grid item xs={12} md={6}>
                <Typography variant="subtitle1">From:</Typography>
                <Typography variant="body1" gutterBottom>Your Account</Typography>
                
                <Divider sx={{ my: 2 }} />
                
                <Typography variant="subtitle1">To:</Typography>
                <Typography variant="body1" gutterBottom>
                  {formData.recipientUsername}
                </Typography>
                
                <Divider sx={{ my: 2 }} />
                
                <Typography variant="subtitle1">Amount:</Typography>
                <Typography variant="h5" gutterBottom>
                  ${parseFloat(formData.amount).toFixed(2)}
                </Typography>
              </Grid>
            </Grid>
          </>
        )}
        
        {activeStep === 2 && (
          <Box sx={{ textAlign: 'center' }}>
            <Alert severity="success" sx={{ mb: 3 }}>
              Your transfer has been successfully processed!
            </Alert>
            
            <Typography variant="h6" gutterBottom>
              ${parseFloat(formData.amount).toFixed(2)} has been sent to {formData.recipientUsername}
            </Typography>
            
            <Typography variant="body1" color="textSecondary" paragraph>
              Transaction ID: {transferResult?.transactionId || 'N/A'}
            </Typography>
          </Box>
        )}
        
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 3 }}>
          {activeStep !== 2 ? (
            <>
              <Button
                disabled={activeStep === 0}
                onClick={handleBack}
              >
                Back
              </Button>
              <Button 
                variant="contained" 
                onClick={handleNext}
                disabled={loading}
              >
                {loading ? (
                  <CircularProgress size={24} />
                ) : activeStep === steps.length - 2 ? 'Confirm Transfer' : 'Next'}
              </Button>
            </>
          ) : (
            <Button 
              variant="contained" 
              onClick={handleReset}
              fullWidth
            >
              Make Another Transfer
            </Button>
          )}
        </Box>
      </Paper>
      
      <ErrorAlert error={error} onClose={() => setError(null)} />
    </>
  );
};

export default Transfer;
