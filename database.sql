CREATE DATABASE IF NOT EXISTS bangla_bank;

USE bangla_bank;

CREATE TABLE IF NOT EXISTS accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    card_number VARCHAR(20) UNIQUE NOT NULL,
    pin VARCHAR(4) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(15) UNIQUE NOT NULL,
    email VARCHAR(100),
    gender VARCHAR(20),
    profession VARCHAR(50),
    nationality VARCHAR(50),
    nid VARCHAR(20),
    address TEXT,
    balance DECIMAL(15, 2) DEFAULT 0.00,
    blocked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
