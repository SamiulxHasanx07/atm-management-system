package com.example.atmmanagementsystem.api.dto;

import com.google.gson.annotations.SerializedName;

public class CardlessDepositRequest {
    @SerializedName("account_number")
    private String accountNumber;

    @SerializedName("nid_proof")
    private String nidProof;

    @SerializedName("amount")
    private double amount;

    public CardlessDepositRequest(String accountNumber, String nidProof, double amount) {
        this.accountNumber = accountNumber;
        this.nidProof = nidProof;
        this.amount = amount;
    }
}
