package com.example.atmmanagementsystem.api.dto;

import com.google.gson.annotations.SerializedName;

public class TransactionResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private TransactionData data;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public TransactionData getData() { return data; }

    public static class TransactionData {
        @SerializedName("transaction")
        private TransactionDetail transaction;

        @SerializedName("balance")
        private double balance;

        public TransactionDetail getTransaction() { return transaction; }
        public double getBalance() { return balance; }
    }

    public static class TransactionDetail {
        @SerializedName("id")
        private int id;

        @SerializedName("card_number")
        private String cardNumber;

        @SerializedName("amount")
        private double amount;

        @SerializedName("transaction_type")
        private String transactionType;

        @SerializedName("timestamp")
        private String timestamp;

        public int getId() { return id; }
        public String getCardNumber() { return cardNumber; }
        public double getAmount() { return amount; }
        public String getTransactionType() { return transactionType; }
        public String getTimestamp() { return timestamp; }
    }
}
