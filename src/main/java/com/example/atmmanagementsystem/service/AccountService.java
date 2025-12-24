package com.example.atmmanagementsystem.service;

import com.example.atmmanagementsystem.model.Account;
import java.util.List;
import java.util.Optional;

public interface AccountService {

    Account createAccount(String name, String phone, double initialDeposit,
            String email, String gender, String profession, String nationality, String nid, String address);

    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByCardNumber(String cardNumber);

    Optional<Account> findByPhone(String phone);

    Optional<Account> findByEmail(String email);

    Optional<Account> findByNid(String nid);

    List<Account> listAccounts();

    void blockAccount(String cardNumber);

    void deposit(String cardNumber, double amount);

    void withdraw(String cardNumber, double amount);

    String resetPin(String cardNumber, String identityProof);

    void updatePin(String cardNumber, String newPin);

    void unblockAccount(String cardNumber);

    List<com.example.atmmanagementsystem.model.Transaction> getTransactions(String cardNumber);

    // Factory method - switching to Database implementation
    static AccountService getInstance() {
        return new DatabaseAccountService();
    }
}
