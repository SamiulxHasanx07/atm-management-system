package com.example.atmmanagementsystem;

import com.example.atmmanagementsystem.api.ApiService;
import com.example.atmmanagementsystem.model.Transaction;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;

public class TestTransactionParsing {
    public static void main(String[] args) {
        String jsonResponse = """
            {
                "success": true,
                "message": "Transactions retrieved",
                "data": [
                    {
                        "id": 3,
                        "card_number": "4450678588410519",
                        "amount": "1000.00",
                        "transaction_type": "DEPOSIT",
                        "timestamp": "2026-04-13T09:41:56.000Z"
                    },
                    {
                        "id": 2,
                        "card_number": "4450678588410519",
                        "amount": "500.00",
                        "transaction_type": "WITHDRAW",
                        "timestamp": "2026-04-13T09:34:45.000Z"
                    },
                    {
                        "id": 1,
                        "card_number": "4450678588410519",
                        "amount": "25000.00",
                        "transaction_type": "DEPOSIT",
                        "timestamp": "2026-04-13T09:34:39.000Z"
                    }
                ]
            }
            """;

        System.out.println("=== Testing Transaction Parsing ===");
        System.out.println("JSON Response:");
        System.out.println(jsonResponse);
        System.out.println("\n" + "=".repeat(50) + "\n");

        try {
            Gson gson = new Gson();
            JsonObject jsonObj = JsonParser.parseString(jsonResponse).getAsJsonObject();
            
            System.out.println("success: " + jsonObj.get("success").getAsBoolean());
            System.out.println("message: " + jsonObj.get("message").getAsString());
            
            var dataArray = jsonObj.getAsJsonArray("data");
            System.out.println("Number of transactions: " + dataArray.size());
            System.out.println();
            
            for (var element : dataArray) {
                var obj = element.getAsJsonObject();
                System.out.println("Transaction:");
                System.out.println("  id: " + obj.get("id").getAsInt());
                System.out.println("  card_number: " + obj.get("card_number").getAsString());
                System.out.println("  amount (raw): " + obj.get("amount").getAsString());
                System.out.println("  amount (parsed): " + Double.parseDouble(obj.get("amount").getAsString()));
                System.out.println("  transaction_type: " + obj.get("transaction_type").getAsString());
                System.out.println("  timestamp: " + obj.get("timestamp").getAsString());
                System.out.println();
            }
            
            // Test actual API call
            System.out.println("\n=== Testing Actual API Call ===");
            ApiService apiService = ApiService.getInstance();
            
            // You need to be logged in first for this to work
            String testCardNumber = "4450678588410519";
            System.out.println("Fetching transactions for card: " + testCardNumber);
            
            // This will only work if you're logged in and have a token
            // The token should be in the session from your login
            List<Transaction> transactions = apiService.getTransactions(testCardNumber);
            System.out.println("Retrieved " + transactions.size() + " transactions");
            
            for (Transaction t : transactions) {
                System.out.println("  - " + t);
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
