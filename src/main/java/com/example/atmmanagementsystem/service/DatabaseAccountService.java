package com.example.atmmanagementsystem.service;

import com.example.atmmanagementsystem.model.Account;
import com.example.atmmanagementsystem.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.security.SecureRandom;

public class DatabaseAccountService implements AccountService {

    private final SecureRandom random = new SecureRandom();

    @Override
    public Account createAccount(String name, String phone, double initialDeposit,
            String email, String gender, String profession, String nationality, String nid, String address) {

        // Validation check for existing phone
        if (findByPhone(phone).isPresent()) {
            throw new IllegalArgumentException("An account with this phone number already exists");
        }

        String acctNum = generateUniqueAccountNumber();
        String cardNum = generateUniqueCardNumber();
        String pin = generatePin();

        String sql = "INSERT INTO accounts (account_number, card_number, pin, name, phone_number, email, gender, profession, nationality, nid, address, balance) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, acctNum);
            pstmt.setString(2, cardNum);

            String hashedPin = com.example.atmmanagementsystem.util.SecurityUtil.hashPin(pin);
            pstmt.setString(3, hashedPin);
            pstmt.setString(4, name);
            pstmt.setString(5, phone);
            pstmt.setString(6, email);
            pstmt.setString(7, gender);
            pstmt.setString(8, profession);
            pstmt.setString(9, nationality);
            pstmt.setString(10, nid);
            pstmt.setString(11, address);
            pstmt.setDouble(12, initialDeposit);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating account failed, no rows affected.");
            }

            Account account = new Account(name, phone, initialDeposit, email, gender, profession, nationality, nid,
                    address);
            account.setAccountNumber(acctNum);
            account.setCardNumber(cardNum);
            // Return plain PIN to the user so they see what it is
            account.setPin(pin);

            return account;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        return getAccountResult(sql, accountNumber);
    }

    @Override
    public Optional<Account> findByCardNumber(String cardNumber) {
        String sql = "SELECT * FROM accounts WHERE card_number = ?";
        return getAccountResult(sql, cardNumber);
    }

    @Override
    public Optional<Account> findByPhone(String phone) {
        if (phone == null)
            return Optional.empty();
        String digits = phone.replaceAll("\\D", "");
        String sql = "SELECT * FROM accounts WHERE phone_number = ?";
        return getAccountResult(sql, digits);
    }

    @Override
    public List<Account> listAccounts() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                accounts.add(mapRowToAccount(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
    }

    private Optional<Account> getAccountResult(String sql, String param) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, param);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToAccount(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private Account mapRowToAccount(ResultSet rs) throws SQLException {
        Account acc = new Account(
                rs.getString("name"),
                rs.getString("phone_number"),
                rs.getDouble("balance"),
                rs.getString("email"),
                rs.getString("gender"),
                rs.getString("profession"),
                rs.getString("nationality"),
                rs.getString("nid"),
                rs.getString("address"));
        acc.setAccountNumber(rs.getString("account_number"));
        acc.setCardNumber(rs.getString("card_number"));
        acc.setPin(rs.getString("pin"));
        acc.setBlocked(rs.getBoolean("blocked"));
        return acc;
    }

    // Helper methods for generation
    private String generateUniqueAccountNumber() {
        for (int i = 0; i < 100; i++) {
            String candidate = String.format("%012d", Math.abs(random.nextLong()) % 1_000_000_000_000L);
            // Check DB for existence
            if (findByAccountNumber(candidate).isEmpty())
                return candidate;
        }
        throw new IllegalStateException("Unable to generate unique account number");
    }

    private String generateUniqueCardNumber() {
        for (int i = 0; i < 100; i++) {
            StringBuilder sb = new StringBuilder(16);
            for (int j = 0; j < 16; j++)
                sb.append(random.nextInt(10));
            String candidate = sb.toString();
            // Check DB for existence
            if (findByCardNumber(candidate).isEmpty())
                return candidate;
        }
        throw new IllegalStateException("Unable to generate unique card number");
    }

    private String generatePin() {
        int p = random.nextInt(10_000);
        return String.format("%04d", p);
    }
}
