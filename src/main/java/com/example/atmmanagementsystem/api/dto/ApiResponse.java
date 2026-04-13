package com.example.atmmanagementsystem.api.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class ApiResponse<T> {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    @SerializedName("error")
    private String error;

    @SerializedName("errors")
    private List<Map<String, String>> errors;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public String getError() { return error; }
    public List<Map<String, String>> getErrors() { return errors; }
}
