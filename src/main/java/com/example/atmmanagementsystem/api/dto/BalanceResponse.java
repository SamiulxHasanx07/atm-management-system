package com.example.atmmanagementsystem.api.dto;

import com.google.gson.annotations.SerializedName;

public class BalanceResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private BalanceData data;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public BalanceData getData() { return data; }

    public static class BalanceData {
        @SerializedName("balance")
        private double balance;

        public double getBalance() { return balance; }
    }
}
