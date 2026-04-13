package com.example.atmmanagementsystem.api;

/**
 * Session manager to store token and user info after login.
 * Acts like localStorage/sessionStorage for the JavaFX application.
 */
public class SessionManager {
    
    private static SessionManager instance;
    
    private String token;
    private String cardNumber;
    private String accountNumber;
    private String name;
    private double balance;
    
    private SessionManager() {
        // Private constructor for singleton
    }
    
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Save session data after successful login
     */
    public void saveSession(String token, String cardNumber, String accountNumber, 
                           String name, double balance) {
        this.token = token;
        this.cardNumber = cardNumber;
        this.accountNumber = accountNumber;
        this.name = name;
        this.balance = balance;
    }
    
    /**
     * Clear all session data (logout/reset)
     */
    public void clearSession() {
        this.token = null;
        this.cardNumber = null;
        this.accountNumber = null;
        this.name = null;
        this.balance = 0.0;
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return token != null && !token.isEmpty();
    }
    
    // Getters
    public String getToken() { return token; }
    public String getCardNumber() { return cardNumber; }
    public String getAccountNumber() { return accountNumber; }
    public String getName() { return name; }
    public double getBalance() { return balance; }
    
    // Setters (for updating balance after transactions)
    public void setToken(String token) { this.token = token; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setName(String name) { this.name = name; }
    public void setBalance(double balance) { this.balance = balance; }
    
    @Override
    public String toString() {
        return "SessionManager{" +
                "cardNumber='" + cardNumber + '\'' +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                '}';
    }
}
