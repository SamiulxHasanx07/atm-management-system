package com.example.atmmanagementsystem.api.dto;

import com.google.gson.annotations.SerializedName;

public class CreateAccountResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private AccountCreationData data;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public AccountCreationData getData() { return data; }

    public static class AccountCreationData {
        @SerializedName("account")
        private AccountInfo account;

        @SerializedName("pin")
        private String pin;

        public AccountInfo getAccount() { return account; }
        public String getPin() { return pin; }
    }

    public static class AccountInfo {
        @SerializedName("account_number")
        private String accountNumber;

        @SerializedName("card_number")
        private String cardNumber;

        @SerializedName("name")
        private String name;

        @SerializedName("phone_number")
        private String phoneNumber;

        @SerializedName("email")
        private String email;

        @SerializedName("gender")
        private String gender;

        @SerializedName("profession")
        private String profession;

        @SerializedName("nationality")
        private String nationality;

        @SerializedName("nid")
        private String nid;

        @SerializedName("address")
        private String address;

        @SerializedName("balance")
        private double balance;

        @SerializedName("created_at")
        private String createdAt;

        public String getAccountNumber() { return accountNumber; }
        public String getCardNumber() { return cardNumber; }
        public String getName() { return name; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getEmail() { return email; }
        public String getGender() { return gender; }
        public String getProfession() { return profession; }
        public String getNationality() { return nationality; }
        public String getNid() { return nid; }
        public String getAddress() { return address; }
        public double getBalance() { return balance; }
        public String getCreatedAt() { return createdAt; }
    }
}
