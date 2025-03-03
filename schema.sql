
-- Create users table
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL, -- Store hashed passwords only
    email VARCHAR(100) UNIQUE NOT NULL,
    balance DECIMAL(12,2) NOT NULL DEFAULT 0.00 CHECK (balance >= 0), -- Prevent negative balance
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create transaction types enum
CREATE TYPE transaction_type AS ENUM ('deposit', 'withdrawal', 'transfer_in', 'transfer_out');

-- Create transactions table
CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(user_id),
    amount DECIMAL(12,2) NOT NULL,
    type transaction_type NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for performance on common queries
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_timestamp ON transactions(timestamp);

-- Function to handle withdrawals safely
CREATE OR REPLACE FUNCTION process_withdrawal()
RETURNS TRIGGER AS $$
DECLARE
    current_balance DECIMAL(12,2);
BEGIN
    -- Only check for withdrawals and transfer_out types
    IF NEW.type = 'withdrawal' OR NEW.type = 'transfer_out' THEN
        SELECT balance INTO current_balance FROM users WHERE user_id = NEW.user_id FOR UPDATE;
        
        IF current_balance < NEW.amount THEN
            RAISE EXCEPTION 'Insufficient funds: available balance is %', current_balance;
        END IF;
        
        -- Update the user's balance
        UPDATE users SET balance = balance - NEW.amount WHERE user_id = NEW.user_id;
    ELSIF NEW.type = 'deposit' OR NEW.type = 'transfer_in' THEN
        -- Handle deposits and incoming transfers
        UPDATE users SET balance = balance + NEW.amount WHERE user_id = NEW.user_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- Function to create a transfer between accounts
CREATE OR REPLACE FUNCTION create_transfer(
    sender_id INTEGER,
    recipient_id INTEGER,
    transfer_amount DECIMAL(12,2),
    transfer_description TEXT
) RETURNS BOOLEAN AS $$
DECLARE
    sender_balance DECIMAL(12,2);
BEGIN
    -- Check if sender has enough funds
    SELECT balance INTO sender_balance FROM users WHERE user_id = sender_id FOR UPDATE;
    
    IF sender_balance < transfer_amount THEN
        RETURN FALSE;
    END IF;
    
    -- Create outgoing transaction
    INSERT INTO transactions (user_id, amount, type, description)
    VALUES (sender_id, transfer_amount, 'transfer_out', transfer_description);
    
    -- Create incoming transaction
    INSERT INTO transactions (user_id, amount, type, description)
    VALUES (recipient_id, transfer_amount, 'transfer_in', transfer_description);
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;
