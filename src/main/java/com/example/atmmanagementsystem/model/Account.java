package com.example.atmmanagementsystem.model;

import java.util.Objects;

/**
 * Simple Account model with basic validation and encapsulation.
 * Assumptions:
 * - phone numbers are 10-15 digits
 * - initial deposit must be >= MIN_INITIAL_DEPOSIT
 */
public class Account {
    public static final double MIN_INITIAL_DEPOSIT = 100.0; // assumption: minimum required

    private String name;
    private String phoneNumber;
    private String accountNumber;
    private String cardNumber;
    private String pin; // 4-digit PIN
    private double balance;
    private boolean blocked = false;

    // constructor: use AccountService to create accounts
    public Account(String name, String phoneNumber, double initialDeposit) {
        setName(name);
        setPhoneNumber(phoneNumber);
        setBalance(initialDeposit);
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getPin() {
        return pin;
    }

    public double getBalance() {
        return balance;
    }

    public boolean isBlocked() {
        return blocked;
    }

    // Setters with validation where appropriate
    public void setName(String name) {
        if (name == null || name.trim().length() < 2) {
            throw new IllegalArgumentException("Name must be at least 2 characters");
        }
        this.name = name.trim();
    }

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number cannot be null");
        }
        String digits = phoneNumber.replaceAll("\\D", "");
        if (digits.length() < 10 || digits.length() > 15) {
            throw new IllegalArgumentException("Phone number must contain 10 to 15 digits");
        }
        this.phoneNumber = digits;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setPin(String pin) {
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("PIN must be exactly 4 digits");
        }
        this.pin = pin;
    }

    public void setBalance(double balance) {
        if (Double.isNaN(balance) || balance < MIN_INITIAL_DEPOSIT) {
            throw new IllegalArgumentException("Initial deposit must be at least " + MIN_INITIAL_DEPOSIT);
        }
        this.balance = balance;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    @Override
    public String toString() {
        return "Account{" +
                "name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", cardNumber='" + cardNumber + '\'' +
                ", pin='" + pin + '\'' +
                ", balance=" + balance +
                ", blocked=" + blocked +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Account account = (Account) o;
        return Objects.equals(phoneNumber, account.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber);
    }
}
