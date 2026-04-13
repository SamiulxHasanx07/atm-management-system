package com.example.atmmanagementsystem.api;

import com.example.atmmanagementsystem.api.dto.*;
import com.example.atmmanagementsystem.model.Account;
import com.example.atmmanagementsystem.model.Transaction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApiService {

    private static ApiService instance;
    private final Gson gson;
    private final HttpClient httpClient;
    private String authToken;

    private ApiService() {
        this.gson = new GsonBuilder().create();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(ApiConfig.CONNECT_TIMEOUT_SECONDS))
                .build();
    }

    public static synchronized ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }

    public void setAuthToken(String token) {
        this.authToken = token;
        // Also save to session
        if (token != null) {
            SessionManager.getInstance().setToken(token);
        }
    }

    public String getAuthToken() {
        // Try to get from session if not set locally
        if (this.authToken == null) {
            this.authToken = SessionManager.getInstance().getToken();
        }
        return authToken;
    }

    public void clearAuthToken() {
        this.authToken = null;
        SessionManager.getInstance().clearSession();
    }

    /**
     * Refresh the session balance by fetching from the backend
     */
    public void refreshSessionBalance(String cardNumber) throws Exception {
        double balance = getBalance(cardNumber);
        SessionManager.getInstance().setBalance(balance);
        System.out.println("Session balance refreshed: " + balance);
    }

    // ==================== Authentication ====================

    public LoginResponse login(String cardNumber, String pin) throws IOException, InterruptedException {
        LoginRequest request = new LoginRequest(cardNumber, pin);
        String jsonBody = gson.toJson(request);

        HttpRequest request_obj = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_URL + "/accounts/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(ApiConfig.READ_TIMEOUT_SECONDS))
                .build();

        HttpResponse<String> response = httpClient.send(request_obj, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            String responseBody = response.body();
            System.out.println("=== LOGIN RESPONSE ===");
            System.out.println("Status: " + response.statusCode());
            System.out.println("Body: " + responseBody);
            System.out.println("======================");

            LoginResponse loginResponse = gson.fromJson(responseBody, LoginResponse.class);
            if (loginResponse.isSuccess() && loginResponse.getToken() != null) {
                this.authToken = loginResponse.getToken();
                
                // Save all user info to session
                if (loginResponse.getAccount() != null) {
                    SessionManager.getInstance().saveSession(
                            loginResponse.getToken(),
                            loginResponse.getAccount().getCardNumber(),
                            loginResponse.getAccount().getAccountNumber(),
                            loginResponse.getAccount().getName(),
                            loginResponse.getAccount().getBalance()
                    );
                    System.out.println("Login successful, session saved: " + SessionManager.getInstance());
                }
            }
            return loginResponse;
        } else {
            try {
                ApiResponse<Object> errorResponse = gson.fromJson(response.body(), ApiResponse.class);
                throw new IOException(errorResponse != null ? errorResponse.getMessage() : "Login failed");
            } catch (Exception e) {
                throw new IOException("Login failed with status: " + response.statusCode());
            }
        }
    }

    // ==================== Accounts ====================

    public Account createAccount(String name, String phone, double initialDeposit,
                                  String email, String gender, String profession,
                                  String nationality, String nid, String address) throws Exception {
        CreateAccountRequest request = new CreateAccountRequest(
                name, phone, email, gender, profession, nationality, nid, address, initialDeposit
        );

        String jsonBody = gson.toJson(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_URL + "/accounts"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(ApiConfig.READ_TIMEOUT_SECONDS))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            CreateAccountResponse createResponse = gson.fromJson(
                    response.body(),
                    CreateAccountResponse.class
            );

            if (createResponse.isSuccess() && createResponse.getData() != null) {
                CreateAccountResponse.AccountInfo accountData = createResponse.getData().getAccount();
                String pin = createResponse.getData().getPin();

                Account account = new Account(name, phone, initialDeposit, email, gender, profession, nationality, nid, address);

                if (accountData != null) {
                    account.setAccountNumber(accountData.getAccountNumber());
                    account.setCardNumber(accountData.getCardNumber());
                }

                if (pin != null) {
                    account.setPin(pin);
                }
                return account;
            } else {
                throw new Exception(createResponse.getMessage());
            }
        } else {
            try {
                ApiResponse<Object> errorResponse = gson.fromJson(response.body(), ApiResponse.class);
                throw new Exception(errorResponse != null ? errorResponse.getMessage() : "Account creation failed");
            } catch (Exception e) {
                throw new Exception("Account creation failed with status: " + response.statusCode());
            }
        }
    }

    public Account getAccountByAccountNumber(String accountNumber) throws Exception {
        // Per API documentation: GET /api/accounts/:accountNumber
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_URL + "/accounts/" + accountNumber))
                .header("Content-Type", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(ApiConfig.READ_TIMEOUT_SECONDS))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            AccountResponse accountResponse = gson.fromJson(
                    response.body(),
                    AccountResponse.class
            );

            if (accountResponse.isSuccess() && accountResponse.getData() != null) {
                AccountResponse.AccountData data = accountResponse.getData();
                Account account = new Account();
                account.setAccountNumber(data.getAccountNumber());
                account.setCardNumber(data.getCardNumber());
                account.setName(data.getName());
                account.setPhoneNumber(data.getPhoneNumber());
                account.setEmail(data.getEmail());
                account.setGender(data.getGender());
                account.setProfession(data.getProfession());
                account.setNationality(data.getNationality());
                account.setNid(data.getNid());
                account.setAddress(data.getAddress());
                account.setBalance(data.getBalance());
                account.setBlocked(false); // Not exposed by this endpoint
                return account;
            }
        }

        throw new Exception("Account not found");
    }

    public double getBalance(String cardNumber) throws Exception {
        String token = getAuthToken(); // Auto-inject from session
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_URL + "/accounts/" + cardNumber + "/balance"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .timeout(Duration.ofSeconds(ApiConfig.READ_TIMEOUT_SECONDS))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            BalanceResponse balanceResponse = gson.fromJson(
                    response.body(),
                    BalanceResponse.class
            );

            if (balanceResponse.isSuccess() && balanceResponse.getData() != null) {
                return balanceResponse.getData().getBalance();
            }
        }

        throw new Exception("Failed to get balance");
    }

    public void blockAccount(String cardNumber, String nidProof) throws Exception {
        String jsonBody = "{\"nid_proof\":\"" + nidProof + "\"}"; // Manually create JSON

        System.out.println("=== API BLOCK ACCOUNT REQUEST ===");
        System.out.println("Card Number: " + cardNumber);
        System.out.println("JSON Body: " + jsonBody);
        System.out.println("================================");

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_URL + "/accounts/" + cardNumber + "/block"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(ApiConfig.READ_TIMEOUT_SECONDS))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println("=== API BLOCK ACCOUNT RESPONSE ===");
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());
        System.out.println("==================================");

        if (response.statusCode() == 200) {
            System.out.println("Account blocked successfully");
            return;
        }

        // Error handling
        String errorMsg = "Failed to block account";
        if (response.body() != null && !response.body().isEmpty()) {
            try {
                ApiResponse<Object> errorResponse = gson.fromJson(response.body(), ApiResponse.class);
                if (errorResponse != null && errorResponse.getMessage() != null) {
                    errorMsg = errorResponse.getMessage();
                }
            } catch (Exception parseEx) {
                System.err.println("Failed to parse error response: " + parseEx.getMessage());
                errorMsg = response.body();
            }
        }
        throw new Exception(errorMsg);
    }

    public void unblockAccount(String cardNumber) throws Exception {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_URL + "/accounts/" + cardNumber + "/unblock"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.ofSeconds(ApiConfig.READ_TIMEOUT_SECONDS))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            try {
                ApiResponse<Object> errorResponse = gson.fromJson(response.body(), ApiResponse.class);
                throw new Exception(errorResponse != null ? errorResponse.getMessage() : "Failed to unblock account");
            } catch (Exception e) {
                throw new Exception("Failed to unblock account with status: " + response.statusCode());
            }
        }
    }

    public void updatePin(String cardNumber, String newPin) throws Exception {
        String token = getAuthToken(); // Auto-inject from session
        String jsonBody = "{\"new_pin\":\"" + newPin + "\"}"; // Manually create JSON

        System.out.println("=== API UPDATE PIN REQUEST ===");
        System.out.println("Card Number: " + cardNumber);
        System.out.println("JSON Body: " + jsonBody);
        System.out.println("================================");

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_URL + "/accounts/" + cardNumber + "/pin"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(ApiConfig.READ_TIMEOUT_SECONDS))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println("=== API UPDATE PIN RESPONSE ===");
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());
        System.out.println("================================");

        if (response.statusCode() == 200) {
            System.out.println("PIN update successful");
            return;
        }

        // Error handling
        String errorMsg = "Failed to update PIN";
        if (response.body() != null && !response.body().isEmpty()) {
            try {
                ApiResponse<Object> errorResponse = gson.fromJson(response.body(), ApiResponse.class);
                if (errorResponse != null && errorResponse.getMessage() != null) {
                    errorMsg = errorResponse.getMessage();
                }
            } catch (Exception parseEx) {
                System.err.println("Failed to parse error response: " + parseEx.getMessage());
                errorMsg = response.body();
            }
        }
        throw new Exception(errorMsg);
    }

    public String resetPin(String cardNumber, String nidProof) throws Exception {
        String jsonBody = "{\"nid_proof\":\"" + nidProof + "\"}"; // Manually create JSON

        System.out.println("=== API RESET PIN REQUEST ===");
        System.out.println("Card Number: " + cardNumber);
        System.out.println("JSON Body: " + jsonBody);
        System.out.println("================================");

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_URL + "/accounts/" + cardNumber + "/pin/reset"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(ApiConfig.READ_TIMEOUT_SECONDS))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println("=== API RESET PIN RESPONSE ===");
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());
        System.out.println("================================");

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            try {
                PinResetResponse pinResetResponse = gson.fromJson(
                        response.body(),
                        PinResetResponse.class
                );

                if (pinResetResponse.isSuccess() && pinResetResponse.getData() != null) {
                    return pinResetResponse.getData().getNewPin();
                }
            } catch (Exception parseEx) {
                System.err.println("Failed to parse PIN reset response: " + parseEx.getMessage());
            }
        }

        throw new Exception("Failed to reset PIN");
    }

    // ==================== Transactions ====================

    public void deposit(String cardNumber, double amount) throws Exception {
        String token = getAuthToken(); // Auto-inject from session
        String jsonBody = "{\"amount\":" + amount + "}"; // Manually create JSON

        System.out.println("=== API DEPOSIT REQUEST ===");
        System.out.println("Card Number: " + cardNumber);
        System.out.println("Amount: " + amount);
        System.out.println("Token: " + (token != null ? token.substring(0, 20) + "..." : "NULL"));
        System.out.println("JSON Body: " + jsonBody);
        System.out.println("URL: " + ApiConfig.BASE_URL + "/transactions/" + cardNumber + "/deposit");
        System.out.println("================================");

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_URL + "/transactions/" + cardNumber + "/deposit"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(ApiConfig.READ_TIMEOUT_SECONDS))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println("=== API DEPOSIT RESPONSE ===");
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());
        System.out.println("==============================");

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            System.out.println("Deposit successful");
            
            // Parse response to get updated balance
            try {
                TransactionResponse transactionResponse = gson.fromJson(
                        response.body(),
                        TransactionResponse.class
                );
                
                if (transactionResponse != null && transactionResponse.getData() != null) {
                    double newBalance = transactionResponse.getData().getBalance();
                    SessionManager.getInstance().setBalance(newBalance);
                    System.out.println("Session balance updated: " + newBalance);
                }
            } catch (Exception parseEx) {
                System.err.println("Failed to parse deposit response: " + parseEx.getMessage());
                // Don't throw - deposit was successful even if we couldn't parse the balance
            }
            
            return;
        }
        
        // Error handling
        String errorMsg = "Deposit failed";
        if (response.body() != null && !response.body().isEmpty()) {
            try {
                ApiResponse<Object> errorResponse = gson.fromJson(response.body(), ApiResponse.class);
                if (errorResponse != null && errorResponse.getMessage() != null) {
                    errorMsg = errorResponse.getMessage();
                }
            } catch (Exception parseEx) {
                System.err.println("Failed to parse error response, using raw body: " + parseEx.getMessage());
                errorMsg = response.body();
            }
        }
        throw new Exception(errorMsg);
    }

    public void withdraw(String cardNumber, double amount) throws Exception {
        String token = getAuthToken(); // Auto-inject from session
        String jsonBody = "{\"amount\":" + amount + "}"; // Manually create JSON

        System.out.println("=== API WITHDRAW REQUEST ===");
        System.out.println("Card Number: " + cardNumber);
        System.out.println("Amount: " + amount);
        System.out.println("Token: " + (token != null ? token.substring(0, 20) + "..." : "NULL"));
        System.out.println("JSON Body: " + jsonBody);
        System.out.println("URL: " + ApiConfig.BASE_URL + "/transactions/" + cardNumber + "/withdraw");
        System.out.println("================================");

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_URL + "/transactions/" + cardNumber + "/withdraw"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(ApiConfig.READ_TIMEOUT_SECONDS))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println("=== API WITHDRAW RESPONSE ===");
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());
        System.out.println("================================");

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            System.out.println("Withdraw successful");
            
            // Parse response to get updated balance
            try {
                TransactionResponse transactionResponse = gson.fromJson(
                        response.body(),
                        TransactionResponse.class
                );
                
                if (transactionResponse != null && transactionResponse.getData() != null) {
                    double newBalance = transactionResponse.getData().getBalance();
                    SessionManager.getInstance().setBalance(newBalance);
                    System.out.println("Session balance updated: " + newBalance);
                }
            } catch (Exception parseEx) {
                System.err.println("Failed to parse withdraw response: " + parseEx.getMessage());
                // Don't throw - withdraw was successful even if we couldn't parse the balance
            }
            
            return;
        }
        
        // Error handling
        String errorMsg = "Withdrawal failed";
        if (response.body() != null && !response.body().isEmpty()) {
            try {
                ApiResponse<Object> errorResponse = gson.fromJson(response.body(), ApiResponse.class);
                if (errorResponse != null && errorResponse.getMessage() != null) {
                    errorMsg = errorResponse.getMessage();
                }
            } catch (Exception parseEx) {
                System.err.println("Failed to parse error response, using raw body: " + parseEx.getMessage());
                errorMsg = response.body();
            }
        }
        throw new Exception(errorMsg);
    }

    public List<Transaction> getTransactions(String cardNumber) throws Exception {
        return getTransactions(cardNumber, null, null, null, null);
    }

    /**
     * Get transaction history for a card with optional filters
     * 
     * @param cardNumber The card number
     * @param type Transaction type filter (e.g., "DEPOSIT", "WITHDRAW")
     * @param dateFrom Start date filter (yyyy-MM-dd format)
     * @param dateTo End date filter (yyyy-MM-dd format)
     * @param limit Maximum number of transactions to return
     */
    public List<Transaction> getTransactions(String cardNumber, String type, 
                                              String dateFrom, String dateTo, 
                                              Integer limit) throws Exception {
        String token = getAuthToken(); // Auto-inject from session

        // Build URL with query parameters
        StringBuilder urlBuilder = new StringBuilder(ApiConfig.BASE_URL + "/transactions/" + cardNumber);
        boolean hasQueryParams = false;
        
        if (type != null || dateFrom != null || dateTo != null || limit != null) {
            urlBuilder.append("?");
            hasQueryParams = true;
            
            boolean first = true;
            if (type != null) {
                urlBuilder.append("type=").append(type);
                first = false;
            }
            if (dateFrom != null) {
                if (!first) urlBuilder.append("&");
                urlBuilder.append("date_from=").append(dateFrom);
                first = false;
            }
            if (dateTo != null) {
                if (!first) urlBuilder.append("&");
                urlBuilder.append("date_to=").append(dateTo);
                first = false;
            }
            if (limit != null) {
                if (!first) urlBuilder.append("&");
                urlBuilder.append("limit=").append(limit);
            }
        }

        System.out.println("=== API GET TRANSACTIONS REQUEST ===");
        System.out.println("Card Number: " + cardNumber);
        System.out.println("Type: " + type);
        System.out.println("Date From: " + dateFrom);
        System.out.println("Date To: " + dateTo);
        System.out.println("Limit: " + limit);
        System.out.println("Token: " + (token != null ? token.substring(0, 20) + "..." : "NULL"));
        System.out.println("Full URL: " + urlBuilder.toString());
        System.out.println("=====================================");

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(urlBuilder.toString()))
                .header("Authorization", "Bearer " + token)
                .GET()
                .timeout(Duration.ofSeconds(ApiConfig.READ_TIMEOUT_SECONDS))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println("=== API GET TRANSACTIONS RESPONSE ===");
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());
        System.out.println("=====================================");

        if (response.statusCode() == 200) {
            try {
                String responseBody = response.body();
                System.out.println("=== Parsing Transaction Response ===");
                System.out.println("Full response body: " + responseBody);
                
                // Parse using JsonObject for manual extraction
                com.google.gson.JsonObject jsonObj = com.google.gson.JsonParser.parseString(responseBody)
                        .getAsJsonObject();
                
                // Check if response is successful
                boolean success = false;
                if (jsonObj.has("success")) {
                    success = jsonObj.get("success").getAsBoolean();
                    System.out.println("Response success: " + success);
                }
                
                // Extract the data array manually
                List<Transaction> result = new ArrayList<>();
                if (jsonObj.has("data") && !jsonObj.get("data").isJsonNull()) {
                    com.google.gson.JsonArray dataArray = jsonObj.getAsJsonArray("data");
                    System.out.println("Found data array with " + dataArray.size() + " items");
                    
                    for (int i = 0; i < dataArray.size(); i++) {
                        com.google.gson.JsonObject itemObj = dataArray.get(i).getAsJsonObject();
                        System.out.println("\nParsing transaction item " + (i + 1) + ":");
                        System.out.println("  Raw JSON: " + itemObj.toString());
                        
                        try {
                            int id = itemObj.has("id") ? itemObj.get("id").getAsInt() : 0;
                            String cardNum = itemObj.has("card_number") ? itemObj.get("card_number").getAsString() : "";
                            
                            // Parse amount - could be string or number
                            double amount = 0.0;
                            if (itemObj.has("amount")) {
                                com.google.gson.JsonElement amountElement = itemObj.get("amount");
                                if (amountElement.isJsonPrimitive()) {
                                    if (amountElement.getAsJsonPrimitive().isString()) {
                                        amount = Double.parseDouble(amountElement.getAsString());
                                    } else {
                                        amount = amountElement.getAsDouble();
                                    }
                                }
                            }
                            
                            String transactionType = itemObj.has("transaction_type") ? itemObj.get("transaction_type").getAsString() : "";
                            String timestamp = itemObj.has("timestamp") ? itemObj.get("timestamp").getAsString() : "";
                            
                            System.out.println("  Parsed values:");
                            System.out.println("    id: " + id);
                            System.out.println("    cardNumber: " + cardNum);
                            System.out.println("    amount: " + amount);
                            System.out.println("    transactionType: " + transactionType);
                            System.out.println("    timestamp: " + timestamp);
                            
                            // Parse timestamp
                            java.sql.Timestamp ts = null;
                            if (timestamp != null && !timestamp.isEmpty()) {
                                try {
                                    String cleaned = timestamp.replace("Z", "").replace("T", " ");
                                    if (cleaned.contains(".")) {
                                        cleaned = cleaned.substring(0, cleaned.indexOf("."));
                                    }
                                    ts = java.sql.Timestamp.valueOf(cleaned);
                                } catch (Exception e) {
                                    System.err.println("    Failed to parse timestamp: " + e.getMessage());
                                    ts = new java.sql.Timestamp(System.currentTimeMillis());
                                }
                            }
                            
                            Transaction txn = new Transaction(id, cardNum, amount, transactionType, ts);
                            result.add(txn);
                            System.out.println("  Successfully parsed transaction: " + txn);
                            
                        } catch (Exception ex) {
                            System.err.println("  Failed to parse transaction item " + (i + 1) + ": " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("No 'data' field found in response");
                }
                
                System.out.println("\nTotal transactions parsed: " + result.size());
                return result;
                
            } catch (Exception parseEx) {
                System.err.println("Failed to parse transactions response: " + parseEx.getMessage());
                parseEx.printStackTrace();
                throw new Exception("Failed to parse transactions: " + parseEx.getMessage());
            }
        }

        throw new Exception("Failed to get transactions - Status: " + response.statusCode());
    }

    /**
     * Verify NID for an account using the verify-nid endpoint
     */
    public boolean verifyNid(String accountNumber, String nidProof) throws Exception {
        String jsonBody = "{\"account_number\":\"" + accountNumber + "\",\"nid_proof\":\"" + nidProof + "\"}";

        System.out.println("=== API VERIFY NID REQUEST ===");
        System.out.println("Account Number: " + accountNumber);
        System.out.println("NID Proof: " + nidProof);
        System.out.println("JSON Body: " + jsonBody);
        System.out.println("URL: " + ApiConfig.BASE_URL + "/accounts/verify-nid");
        System.out.println("================================");

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_URL + "/accounts/verify-nid"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(ApiConfig.READ_TIMEOUT_SECONDS))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println("=== API VERIFY NID RESPONSE ===");
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());
        System.out.println("================================");

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            System.out.println("NID verification successful");
            return true;
        }

        // Error handling - parse validation errors
        String errorMsg = "NID verification failed";
        if (response.body() != null && !response.body().isEmpty()) {
            try {
                ApiResponse<Object> errorResponse = gson.fromJson(response.body(), ApiResponse.class);
                if (errorResponse != null) {
                    if (errorResponse.getMessage() != null) {
                        errorMsg = errorResponse.getMessage();
                    }
                    // Check for field-specific errors
                    if (errorResponse.getErrors() != null && !errorResponse.getErrors().isEmpty()) {
                        // Get the first error message
                        Map<String, String> firstError = errorResponse.getErrors().get(0);
                        if (firstError.containsKey("message")) {
                            errorMsg = firstError.get("message");
                        } else if (firstError.containsKey("field")) {
                            errorMsg = firstError.get("field") + " validation failed";
                        }
                    }
                }
            } catch (Exception parseEx) {
                System.err.println("Failed to parse error response: " + parseEx.getMessage());
                errorMsg = response.body();
            }
        }
        throw new Exception(errorMsg);
    }

    public void cardlessDeposit(String accountNumber, String nidProof, double amount) throws Exception {
        String jsonBody = "{\"account_number\":\"" + accountNumber + "\",\"nid_proof\":\"" + nidProof + "\",\"amount\":" + amount + "}";

        System.out.println("==========================================");
        System.out.println("=== API CARDLESS DEPOSIT REQUEST ===");
        System.out.println("Account Number: " + accountNumber);
        System.out.println("NID Proof: " + nidProof);
        System.out.println("Amount: " + amount);
        System.out.println("JSON Body: " + jsonBody);
        System.out.println("URL: " + ApiConfig.BASE_URL + "/transactions/cardless-deposit");
        System.out.println("==========================================");

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_URL + "/transactions/cardless-deposit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(ApiConfig.READ_TIMEOUT_SECONDS))
                .build();

        System.out.println("Sending HTTP request...");
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println("==========================================");
        System.out.println("=== API CARDLESS DEPOSIT RESPONSE ===");
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());
        System.out.println("==========================================");

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            System.out.println("Cardless deposit HTTP request successful");
            
            // Parse response to get updated balance
            try {
                TransactionResponse transactionResponse = gson.fromJson(
                        response.body(),
                        TransactionResponse.class
                );
                
                if (transactionResponse != null && transactionResponse.getData() != null) {
                    double newBalance = transactionResponse.getData().getBalance();
                    SessionManager.getInstance().setBalance(newBalance);
                    System.out.println("Session balance updated from response: " + newBalance);
                } else {
                    System.out.println("Transaction response data was null");
                }
            } catch (Exception parseEx) {
                System.err.println("Failed to parse cardless deposit response (non-critical): " + parseEx.getMessage());
                parseEx.printStackTrace();
            }
            
            return;
        }

        // Error handling
        String errorMsg = "Cardless deposit failed";
        if (response.body() != null && !response.body().isEmpty()) {
            try {
                ApiResponse<Object> errorResponse = gson.fromJson(response.body(), ApiResponse.class);
                if (errorResponse != null && errorResponse.getMessage() != null) {
                    errorMsg = errorResponse.getMessage();
                    System.err.println("API Error: " + errorMsg);
                }
            } catch (Exception parseEx) {
                System.err.println("Failed to parse error response, using raw body");
                errorMsg = response.body();
            }
        }
        throw new Exception(errorMsg);
    }

    // ==================== Helper Methods ====================

    private Transaction convertToTransaction(TransactionListResponse.TransactionItem item) {
        System.out.println("Converting transaction item:");
        System.out.println("  ID: " + item.getId());
        System.out.println("  Card Number: " + item.getCardNumber());
        System.out.println("  Amount: " + item.getAmount());
        System.out.println("  Transaction Type: " + item.getTransactionType());
        System.out.println("  Timestamp: " + item.getTimestamp());
        
        java.sql.Timestamp timestamp = null;
        try {
            String tsStr = item.getTimestamp();
            if (tsStr != null && !tsStr.isEmpty()) {
                // Handle ISO 8601 format: "2026-04-08T12:00:00.000Z"
                // Remove the 'Z' and replace 'T' with space for SQL timestamp
                String cleaned = tsStr.replace("Z", "").replace("T", " ");
                // Remove milliseconds decimal if present for simpler parsing
                if (cleaned.contains(".")) {
                    cleaned = cleaned.substring(0, cleaned.indexOf("."));
                }
                timestamp = java.sql.Timestamp.valueOf(cleaned);
            }
        } catch (Exception e) {
            System.err.println("Failed to parse timestamp: " + item.getTimestamp() + " - " + e.getMessage());
            timestamp = new java.sql.Timestamp(System.currentTimeMillis()); // Fallback to now
        }

        Transaction result = new Transaction(
                item.getId(),
                item.getCardNumber(),
                item.getAmount(),
                item.getTransactionType(),
                timestamp
        );
        System.out.println("  Converted Transaction: " + result);
        return result;
    }

    // ==================== Inner DTO Classes ====================

    private static class PinUpdateRequest {
        @com.google.gson.annotations.SerializedName("new_pin")
        private String newPin;

        PinUpdateRequest(String newPin) {
            this.newPin = newPin;
        }
    }

    private static class PinResetRequest {
        @com.google.gson.annotations.SerializedName("nid_proof")
        private String nidProof;

        PinResetRequest(String nidProof) {
            this.nidProof = nidProof;
        }
    }

    // Separate DTO class for transaction requests
    private static class TransactionRequest {
        @com.google.gson.annotations.SerializedName("amount")
        private double amount;

        TransactionRequest(double amount) {
            this.amount = amount;
        }
        
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
    }

    private static class TransactionListResponse {
        @com.google.gson.annotations.SerializedName("success")
        private boolean success;

        @com.google.gson.annotations.SerializedName("message")
        private String message;

        @com.google.gson.annotations.SerializedName("data")
        private List<TransactionItem> data;

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public List<TransactionItem> getData() { return data; }

        static class TransactionItem {
            @com.google.gson.annotations.SerializedName("id")
            private int id;

            @com.google.gson.annotations.SerializedName("card_number")
            private String cardNumber;

            @com.google.gson.annotations.SerializedName("amount")
            private String amount; // API returns amount as string

            @com.google.gson.annotations.SerializedName("transaction_type")
            private String transactionType;

            @com.google.gson.annotations.SerializedName("timestamp")
            private String timestamp;

            public int getId() { return id; }
            public String getCardNumber() { return cardNumber; }
            
            /**
             * Gets the amount as a double value.
             * Parses from string since API returns amount as string.
             */
            public double getAmount() { 
                try {
                    return amount != null ? Double.parseDouble(amount) : 0.0;
                } catch (NumberFormatException e) {
                    System.err.println("Failed to parse amount: " + amount + " - " + e.getMessage());
                    return 0.0;
                }
            }
            
            public String getTransactionType() { return transactionType; }
            public String getTimestamp() { return timestamp; }
        }
    }
}
