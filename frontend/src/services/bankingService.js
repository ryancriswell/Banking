import api from './api';

export const fetchBalance = async () => {
  try {
    const response = await api.get('/banking/v1/balance');
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const fetchTransactions = async (page = 0, size = 10) => {
  try {
    const response = await api.get(`/banking/v1/transactions`, {
      params: { page, size }
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const transferFunds = async (recipientUsername, amount) => {
  try {
    const response = await api.post('/banking/v1/transfer', {
      recipientUsername,
      amount: Number(amount)
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};
