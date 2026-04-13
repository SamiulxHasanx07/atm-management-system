package com.example.atmmanagementsystem.api.dto;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("card_number")
    private String cardNumber;
    
    @SerializedName("pin")
    private String pin;

    public LoginRequest(String cardNumber, String pin) {
        this.cardNumber = cardNumber;
        this.pin = pin;
    }
}
