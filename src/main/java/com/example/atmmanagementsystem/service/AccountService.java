package com.example.atmmanagementsystem.service;

import com.example.atmmanagementsystem.model.Account;

import java.security.SecureRandom;
import java.util.*;

public class AccountService {
    private static final AccountService INSTANCE = new AccountService();

    public static AccountService getInstance() {
        return INSTANCE;
    }
    private final Map<String, Account> byAccountNumber = new HashMap<>();
    private final Map<String, Account> byCardNumber = new HashMap<>();
    private final Map<String, Account> byPhone = new HashMap<>();

    private final SecureRandom random = new SecureRandom();

    // make constructor default (public) to preserve existing usage, but preferred access is via getInstance()

    public synchronized Account createAccount(String name, String phone, double initialDeposit) {
        Account account = new Account(name, phone, initialDeposit);

        if (byPhone.containsKey(account.getPhoneNumber())) {
            throw new IllegalArgumentException("An account with this phone number already exists");
        }

        // Assign unique account number, card number and PIN
        String acctNum = generateUniqueAccountNumber();
        String cardNum = generateUniqueCardNumber();
        String pin = generatePin();

        account.setAccountNumber(acctNum);
        account.setCardNumber(cardNum);
        account.setPin(pin);

        // store
        byAccountNumber.put(acctNum, account);
        byCardNumber.put(cardNum, account);
        byPhone.put(account.getPhoneNumber(), account);

        return account;
    }

    public Optional<Account> findByAccountNumber(String accountNumber) {
        return Optional.ofNullable(byAccountNumber.get(accountNumber));
    }

    public Optional<Account> findByCardNumber(String cardNumber) {
        return Optional.ofNullable(byCardNumber.get(cardNumber));
    }

    public Optional<Account> findByPhone(String phone) {
        if (phone == null) return Optional.empty();
        String digits = phone.replaceAll("\\D", "");
        return Optional.ofNullable(byPhone.get(digits));
    }

    public List<Account> listAccounts() {
        return new ArrayList<>(byAccountNumber.values());
    }

    // Simple format: account number = 12 digits, card number = 16 digits
    private String generateUniqueAccountNumber() {
        for (int i = 0; i < 10_000; i++) {
            String candidate = String.format("%012d", Math.abs(random.nextLong()) % 1_000_000_000_000L);
            if (!byAccountNumber.containsKey(candidate)) return candidate;
        }
        throw new IllegalStateException("Unable to generate unique account number");
    }

    private String generateUniqueCardNumber() {
        for (int i = 0; i < 10_000; i++) {
            // generate 16-digit number
            StringBuilder sb = new StringBuilder(16);
            for (int j = 0; j < 16; j++) sb.append(random.nextInt(10));
            String candidate = sb.toString();
            if (!byCardNumber.containsKey(candidate)) return candidate;
        }
        throw new IllegalStateException("Unable to generate unique card number");
    }

    private String generatePin() {
        int p = random.nextInt(10_000);
        return String.format("%04d", p);
    }
}
