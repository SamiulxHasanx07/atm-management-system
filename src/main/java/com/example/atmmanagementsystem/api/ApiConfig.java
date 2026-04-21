package com.example.atmmanagementsystem.api;

public class ApiConfig {
    // For Development Environment
    // public static final String BASE_URL = "http://localhost:5000/api";

    // For Production Environment
    public static final String BASE_URL = "https://atm-management-system-backend.vercel.app/api";
    public static final int CONNECT_TIMEOUT_SECONDS = 10;
    public static final int READ_TIMEOUT_SECONDS = 30;
}
