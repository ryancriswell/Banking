import React, { useState, useEffect } from 'react';
import { 
  Typography, Paper, TableContainer, Table, TableHead, 
  TableBody, TableRow, TableCell, TablePagination, 
  Box, CircularProgress, Chip
} from '@mui/material';
import { fetchTransactions } from '../services/bankingService';
import ErrorAlert from '../components/ErrorAlert';
import { formatTransaction, transactionStatusColor } from '../components/TransactionFormatter';

const TransactionHistory = () => {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalCount, setTotalCount] = useState(0);

  useEffect(() => {
    const loadTransactions = async () => {
      setLoading(true);
      setError(null);
      
      try {
        const response = await fetchTransactions(page, rowsPerPage);
        setTransactions(response.content || []);
        setTotalCount(response.totalElements || 0);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load transactions');
        console.error('Transaction load error:', err);
      } finally {
        setLoading(false);
      }
    };

    loadTransactions();
  }, [page, rowsPerPage]);

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  if (loading && page === 0) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <>
      <Typography variant="h4" gutterBottom>
        Transaction History
      </Typography>
      
      <Paper sx={{ width: '100%', overflow: 'hidden' }}>
        <TableContainer sx={{ maxHeight: 440 }}>
          <Table stickyHeader aria-label="transaction table">
            <TableHead>
              <TableRow>
                <TableCell>Date</TableCell>
                <TableCell>Time</TableCell>
                <TableCell>Type</TableCell>
                <TableCell align="right">Amount</TableCell>
                <TableCell align="right">Remaining Balance</TableCell>
                <TableCell>Status</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {transactions.length > 0 ? (
                transactions.map((transaction, index) => (
                  <TableRow hover key={index}>
                    <TableCell>
                      {new Date(transaction.timestamp).toLocaleDateString()}
                    </TableCell>
                    <TableCell>
                      {new Date(transaction.timestamp).toLocaleTimeString()}
                    </TableCell>
                    <TableCell>
                      {transaction.type}
                    </TableCell>
                    <TableCell align="right" sx={{ 
                      color: formatTransaction(transaction).color,
                      fontWeight: 'bold'
                    }}>
                      {formatTransaction(transaction).currency}
                    </TableCell>
                    <TableCell align="right">
                      {`$${transaction.balanceAfter.toFixed(2)}`}
                    </TableCell>
                    <TableCell>
                      <Chip 
                        label={transaction.status} 
                        color={transactionStatusColor(transaction.status)}
                        size="small"
                      />
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={5} align="center">
                    No transactions found
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
        <TablePagination
          rowsPerPageOptions={[5, 10, 25]}
          component="div"
          count={totalCount}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={handleChangePage}
          onRowsPerPageChange={handleChangeRowsPerPage}
        />
      </Paper>
      <ErrorAlert error={error} onClose={() => setError(null)} />
    </>
  );
};

export default TransactionHistory;
