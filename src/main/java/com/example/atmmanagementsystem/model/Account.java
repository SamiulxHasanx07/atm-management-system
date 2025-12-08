package com.example.atmmanagementsystem.model;

import java.util.Objects;

/**
 * Simple Account model with basic validation and encapsulation.
 * Assumptions:
 * - phone numbers are 10-15 digits
 * - initial deposit must be >= MIN_INITIAL_DEPOSIT
 */
public class Account {
    public static final double MIN_INITIAL_DEPOSIT = 100.0; // assumption: minimum required

    private String name;
    private String phoneNumber;
    private String accountNumber;
    private String cardNumber;
    private String pin; // 4-digit PIN
    private double balance;
    private boolean blocked = false;

    private String email;
    private String gender;
    private String profession;
    private String nationality;
    private String nid;
    private String address;

    // constructor: use AccountService to create accounts
    public Account(String name, String phoneNumber, double initialDeposit,
            String email, String gender, String profession, String nationality, String nid, String address) {
        setName(name);
        setPhoneNumber(phoneNumber);
        setBalance(initialDeposit);
        setEmail(email);
        setGender(gender);
        setProfession(profession);
        setNationality(nationality);
        setNid(nid);
        setAddress(address);
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getPin() {
        return pin;
    }

    public double getBalance() {
        return balance;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public String getProfession() {
        return profession;
    }

    public String getNationality() {
        return nationality;
    }

    public String getNid() {
        return nid;
    }

    public String getAddress() {
        return address;
    }

    // Setters with validation where appropriate
    public void setName(String name) {
        if (name == null || name.trim().length() < 2) {
            throw new IllegalArgumentException("Name must be at least 2 characters");
        }
        this.name = name.trim();
    }

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number cannot be null");
        }
        String digits = phoneNumber.replaceAll("\\D", "");
        if (digits.length() < 10 || digits.length() > 15) {
            throw new IllegalArgumentException("Phone number must contain 10 to 15 digits");
        }
        this.phoneNumber = digits;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setPin(String pin) {
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("PIN must be exactly 4 digits");
        }
        this.pin = pin;
    }

    public void setBalance(double balance) {
        if (Double.isNaN(balance) || balance < MIN_INITIAL_DEPOSIT) {
            throw new IllegalArgumentException("Initial deposit must be at least " + MIN_INITIAL_DEPOSIT);
        }
        this.balance = balance;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }
        this.email = email.trim();
    }

    public void setGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            throw new IllegalArgumentException("Gender is required");
        }
        this.gender = gender.trim();
    }

    public void setProfession(String profession) {
        if (profession == null || profession.trim().isEmpty()) {
            throw new IllegalArgumentException("Profession is required");
        }
        this.profession = profession.trim();
    }

    public void setNationality(String nationality) {
        if (nationality == null || nationality.trim().isEmpty()) {
            this.nationality = "Bangladeshi";
        } else {
            this.nationality = nationality.trim();
        }
    }

    public void setNid(String nid) {
        if (nid == null || !nid.matches("\\d+")) {
            throw new IllegalArgumentException("NID must be numeric");
        }
        this.nid = nid;
    }

    public void setAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address is required");
        }
        this.address = address.trim();
    }

    @Override
    public String toString() {
        return "Account{" +
                "name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", gender='" + gender + '\'' +
                ", profession='" + profession + '\'' +
                ", nationality='" + nationality + '\'' +
                ", nid='" + nid + '\'' +
                ", address='" + address + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", cardNumber='" + cardNumber + '\'' +
                ", pin='" + pin + '\'' +
                ", balance=" + balance +
                ", blocked=" + blocked +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Account account = (Account) o;
        return Objects.equals(phoneNumber, account.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber);
    }
}
