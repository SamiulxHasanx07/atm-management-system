package com.example.atmmanagementsystem.service;

import com.example.atmmanagementsystem.api.ApiService;
import com.example.atmmanagementsystem.api.dto.LoginResponse;
import com.example.atmmanagementsystem.model.Account;
import com.example.atmmanagementsystem.model.Transaction;

import java.util.List;
import java.util.Optional;

public class ApiAccountService implements AccountService {

    private final ApiService apiService = ApiService.getInstance();

    @Override
    public Account createAccount(String name, String phone, double initialDeposit,
            String email, String gender, String profession, String nationality, String nid, String address) {
        try {
            return apiService.createAccount(name, phone, initialDeposit, email, gender, 
                    profession, nationality, nid, address);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        // API doesn't have direct account number lookup - would need to implement in backend
        throw new UnsupportedOperationException("Find by account number not supported in API");
    }

    @Override
    public Optional<Account> findByCardNumber(String cardNumber) {
        try {
            Account account = apiService.getAccountByAccountNumber(cardNumber);
            return Optional.of(account);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void refreshSessionBalance(String cardNumber) {
        try {
            apiService.refreshSessionBalance(cardNumber);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<Account> findByPhone(String phone) {
        throw new UnsupportedOperationException("Find by phone not supported in API");
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        throw new UnsupportedOperationException("Find by email not supported in API");
    }

    @Override
    public Optional<Account> findByNid(String nid) {
        throw new UnsupportedOperationException("Find by NID not supported in API");
    }

    @Override
    public List<Account> listAccounts() {
        throw new UnsupportedOperationException("List accounts not supported in API");
    }

    @Override
    public void blockAccount(String cardNumber, String nidProof) {
        try {
            apiService.blockAccount(cardNumber, nidProof);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void deposit(String cardNumber, double amount) {
        try {
            apiService.deposit(cardNumber, amount);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void withdraw(String cardNumber, double amount) {
        try {
            apiService.withdraw(cardNumber, amount);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public String resetPin(String cardNumber, String nidProof) {
        try {
            return apiService.resetPin(cardNumber, nidProof);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void updatePin(String cardNumber, String newPin) {
        try {
            apiService.updatePin(cardNumber, newPin);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void unblockAccount(String cardNumber) {
        try {
            apiService.unblockAccount(cardNumber);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> getTransactions(String cardNumber) {
        try {
            return apiService.getTransactions(cardNumber);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Get transaction history with optional filters
     * 
     * @param cardNumber The card number
     * @param type Transaction type filter (e.g., "DEPOSIT", "WITHDRAW")
     * @param dateFrom Start date filter (yyyy-MM-dd format)
     * @param dateTo End date filter (yyyy-MM-dd format)
     * @param limit Maximum number of transactions to return
     */
    public List<Transaction> getTransactions(String cardNumber, String type, 
                                              String dateFrom, String dateTo, 
                                              Integer limit) {
        try {
            return apiService.getTransactions(cardNumber, type, dateFrom, dateTo, limit);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void cardlessDeposit(String accountNumber, String nidProof, double amount) {
        try {
            apiService.cardlessDeposit(accountNumber, nidProof, amount);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public boolean verifyNid(String accountNumber, String nidProof) {
        try {
            return apiService.verifyNid(accountNumber, nidProof);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    // Helper method for login (not in AccountService interface but needed for authentication)
    public LoginResponse login(String cardNumber, String pin) {
        try {
            return apiService.login(cardNumber, pin);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
