package com.example.atmmanagementsystem.api.dto;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class TransactionData {
    @SerializedName("id")
    private int id;
    
    @SerializedName("card_number")
    private String cardNumber;
    
    @SerializedName("amount")
    private double amount;
    
    @SerializedName("transaction_type")
    private String transactionType;
    
    @SerializedName("timestamp")
    private Date timestamp;

    public int getId() { return id; }
    public String getCardNumber() { return cardNumber; }
    public double getAmount() { return amount; }
    public String getTransactionType() { return transactionType; }
    public Date getTimestamp() { return timestamp; }
}
