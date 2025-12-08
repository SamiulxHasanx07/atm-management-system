package com.example.atmmanagementsystem.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseInitializer {

    private static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "bangla_bank";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static void initialize() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 1. Create Database if not exists
            try (Connection conn = DriverManager.getConnection(BASE_URL, USER, PASSWORD);
                    Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            }

            // 2. Create Table
            try (Connection conn = DriverManager.getConnection(BASE_URL + DB_NAME, USER, PASSWORD);
                    Statement stmt = conn.createStatement()) {

                String sql = "CREATE TABLE IF NOT EXISTS accounts (" +
                        "account_number VARCHAR(20) PRIMARY KEY, " +
                        "card_number VARCHAR(20) UNIQUE NOT NULL, " +
                        "pin VARCHAR(64) NOT NULL, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "phone_number VARCHAR(15) UNIQUE NOT NULL, " +
                        "email VARCHAR(100) UNIQUE, " +
                        "gender VARCHAR(20), " +
                        "profession VARCHAR(50), " +
                        "nationality VARCHAR(50), " +
                        "nid VARCHAR(20) UNIQUE, " +
                        "address TEXT, " +
                        "balance DECIMAL(15, 2) DEFAULT 0.00, " +
                        "blocked BOOLEAN DEFAULT FALSE, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")";
                stmt.execute(sql);

                // 3. Ensure Schema Compatibility (force update PIN column if it exists with old
                // length)
                try {
                    stmt.execute("ALTER TABLE accounts MODIFY COLUMN pin VARCHAR(64) NOT NULL");
                } catch (SQLException e) {
                    // Ignore
                }
            }

            System.out.println("Database and tables initialized successfully.");

        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
