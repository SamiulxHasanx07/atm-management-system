package com.example.atmmanagementsystem.api.dto;

import com.google.gson.annotations.SerializedName;

public class PinResetResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private PinResetData data;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public PinResetData getData() { return data; }

    public static class PinResetData {
        @SerializedName("new_pin")
        private String newPin;

        public String getNewPin() { return newPin; }
    }
}
