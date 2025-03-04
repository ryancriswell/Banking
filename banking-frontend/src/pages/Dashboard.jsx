import React, { useState, useEffect } from 'react';
import { 
  Typography, Card, CardContent, Grid, Box,
  Paper, Button, CircularProgress, useTheme 
} from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import { fetchBalance, fetchTransactions } from '../services/bankingService';
import ErrorAlert from '../components/ErrorAlert';

const Dashboard = () => {
  const [balance, setBalance] = useState(null);
  const [recentTransactions, setRecentTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const theme = useTheme();

  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      setError(null);
      
      try {
        // Fetch balance and recent transactions
        const balanceData = await fetchBalance();
        const transactionsData = await fetchTransactions(0, 5); // Get only 5 recent transactions
        
        setBalance(balanceData.balance);
        setRecentTransactions(transactionsData.transactions || []);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load dashboard data');
        console.error('Dashboard error:', err);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <>
      <Typography variant="h4" gutterBottom>
        Dashboard
      </Typography>
      
      <Grid container spacing={3}>
        {/* Balance Card */}
        <Grid item xs={12} md={6}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Typography variant="h6" color="textSecondary" gutterBottom>
                Current Balance
              </Typography>
              <Typography variant="h3" component="div">
                ${balance !== null ? balance.toFixed(2) : '---'}
              </Typography>
              <Box mt={2}>
                <Button 
                  variant="contained" 
                  color="primary"
                  component={RouterLink}
                  to="/transfer"
                >
                  Transfer Money
                </Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Recent Transactions Card */}
        <Grid item xs={12} md={6}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Typography variant="h6" color="textSecondary" gutterBottom>
                Recent Transactions
              </Typography>
              
              {recentTransactions.length > 0 ? (
                <>
                  {recentTransactions.map((transaction, index) => (
                    <Paper 
                      key={index}
                      elevation={0}
                      sx={{ 
                        p: 1, 
                        mb: 1,
                        borderLeft: '4px solid',
                        borderColor: transaction.amount > 0 ? 
                          theme.palette.success.main : 
                          theme.palette.error.main
                      }}
                    >
                      <Grid container justifyContent="space-between" alignItems="center">
                        <Grid item>
                          <Typography variant="body2">
                            {transaction.description || 
                              (transaction.amount > 0 ? 'Received' : 'Sent')}
                          </Typography>
                          <Typography variant="caption" color="textSecondary">
                            {new Date(transaction.timestamp).toLocaleDateString()}
                          </Typography>
                        </Grid>
                        <Grid item>
                          <Typography 
                            variant="body1" 
                            color={transaction.amount > 0 ? 'success.main' : 'error.main'}
                          >
                            {transaction.amount > 0 ? '+' : ''}{transaction.amount.toFixed(2)}
                          </Typography>
                        </Grid>
                      </Grid>
                    </Paper>
                  ))}
                  <Box mt={2} display="flex" justifyContent="flex-end">
                    <Button 
                      component={RouterLink}
                      to="/transactions"
                      color="primary"
                    >
                      View All Transactions
                    </Button>
                  </Box>
                </>
              ) : (
                <Typography variant="body2" color="textSecondary">
                  No recent transactions
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
      <ErrorAlert error={error} onClose={() => setError(null)} />
    </>
  );
};

export default Dashboard;
