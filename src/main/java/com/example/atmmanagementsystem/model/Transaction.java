package com.example.atmmanagementsystem.model;

import java.sql.Timestamp;

public class Transaction {
    private int id;
    private String cardNumber;
    private double amount;
    private String type; // DEPOSIT, WITHDRAW
    private Timestamp timestamp;

    public Transaction(int id, String cardNumber, double amount, String type, Timestamp timestamp) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
