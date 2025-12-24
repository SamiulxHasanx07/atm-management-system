CREATE DATABASE IF NOT EXISTS bangla_bank;

USE bangla_bank;

CREATE TABLE IF NOT EXISTS accounts (
    account_number VARCHAR(20) PRIMARY KEY,
    card_number VARCHAR(20) UNIQUE NOT NULL,
    pin VARCHAR(64) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(15) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE,
    gender VARCHAR(20),
    profession VARCHAR(50),
    nationality VARCHAR(50),
    nid VARCHAR(20) UNIQUE,
    address TEXT,
    balance DECIMAL(15, 2) DEFAULT 0.00,
    blocked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    card_number VARCHAR(20) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (card_number) REFERENCES accounts(card_number)
);
