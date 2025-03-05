export const formatTransaction = (transaction) => {
  const postiveTransaction = {
    color: 'primary.main',
    currency: `$${transaction.amount.toFixed(2)}`
  }
  const negativeTransaction = {
    color: 'error.main',
    currency: `-$${transaction.amount.toFixed(2)}`
  }

  switch (transaction.type) {
    case "DEPOSIT":
      return postiveTransaction
    case "WITHDRAWAL":
      return negativeTransaction
    case "TRANSFER_IN":
      return postiveTransaction
    case "TRANSFER_OUT":
      return negativeTransaction
    default:
      return postiveTransaction
  }
}

export const transactionStatusColor = (status) => {
  switch (status) {
    case 'PENDING':
      return 'warning'
    case 'COMPLETED':
      return 'primary'
    default:
      return 'error'
  } 
}

// Hack for the border color, could be better
export const transactionBorderColor = (status) => {
  switch (status) {
    case 'PENDING':
      return 'warning.main'
    case 'COMPLETED':
      return 'primary.main'
    default:
      return 'error.main'
  } 
}

