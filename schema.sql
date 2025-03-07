-- Create users table
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL, -- Store hashed passwords only
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- TODO: Use ENUM type for transaction_type after getting ORM to work with it
CREATE TYPE transaction_type AS ENUM ('deposit', 'withdrawal', 'transfer_in', 'transfer_out');

-- Create transactions table
CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(user_id),
    amount BIGINT NOT NULL, -- Store amount in cents
    transaction_type VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "status" VARCHAR(20) NOT NULL
);

-- TEMPORARILY DISABLED TRIGGER TO ALLOW FAILED TRANSACTIONS FOR TESTING

-- Function to validate withdrawals safely, not allowing negative balance. NOT responsible for updating balances
-- CREATE OR REPLACE FUNCTION validate_withdrawal()
-- RETURNS TRIGGER AS $$
-- DECLARE
--     current_balance DECIMAL(12,2);
--     new_balance DECIMAL(12,2);
-- BEGIN
--     -- Only check for withdrawals and transfer_out types, TODO: rely on enum not string comparison
--     IF NEW.transaction_type = 'WITHDRAWAL' OR NEW.transaction_type = 'TRANSFER_OUT' THEN
--         SELECT balance INTO current_balance FROM users WHERE user_id = NEW.user_id FOR UPDATE;
        
--         -- Raise exception to exit if insufficient funds to complete withdrawal
--         IF current_balance < NEW.amount THEN
--             RAISE EXCEPTION 'Insufficient funds: available balance is %', current_balance;
--         END IF;
--     END IF;
    
--     RETURN NEW;
-- END;
-- $$ LANGUAGE plpgsql;

-- -- Trigger to enforce transactional integrity
-- CREATE TRIGGER ensure_transaction_integrity
-- BEFORE INSERT ON transactions
-- FOR EACH ROW
-- EXECUTE FUNCTION validate_withdrawal();

