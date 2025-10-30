package com.example.atmmanagementsystem;

import com.example.atmmanagementsystem.model.Account;
import com.example.atmmanagementsystem.service.AccountService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.List;

public class BanglaBankController {
    @FXML
    private TextField nameField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField depositField;

    @FXML
    private Label errorLabel;

    @FXML
    private TextArea outputArea;

    // keep a single service instance so stored accounts persist while app runs
    private final AccountService service = new AccountService();

    @FXML
    protected void onCreateAccount() {
        errorLabel.setText("");
        String name = nameField.getText();
        String phone = phoneField.getText();
        String depositText = depositField.getText();

        // Basic client-side validation
        if (name == null || name.trim().length() < 2) {
            errorLabel.setText("Name must be at least 2 characters");
            return;
        }

        if (phone == null || phone.replaceAll("\\D", "").length() < 10) {
            errorLabel.setText("Phone must contain at least 10 digits");
            return;
        }

        double deposit;
        try {
            String normalized = depositText == null ? "" : depositText.trim().replace(',', '.');
            deposit = Double.parseDouble(normalized);
        } catch (Exception e) {
            errorLabel.setText("Invalid deposit amount");
            return;
        }

        if (deposit < Account.MIN_INITIAL_DEPOSIT) {
            errorLabel.setText(String.format("Initial deposit must be at least %.2f", Account.MIN_INITIAL_DEPOSIT));
            return;
        }

        try {
            Account acc = service.createAccount(name, phone, deposit);
            StringBuilder out = new StringBuilder();
            out.append("Account created successfully:\n");
            out.append("Name: ").append(acc.getName()).append("\n");
            out.append("Account#: ").append(acc.getAccountNumber()).append("\n");
            out.append("Card#:    ").append(acc.getCardNumber()).append("\n");
            out.append("PIN:      ").append(acc.getPin()).append("\n");
            out.append("Balance:  ").append(String.format("%.2f", acc.getBalance())).append("\n\n");

            // also list stored accounts
            List<Account> accounts = service.listAccounts();
            out.append("Stored accounts (count=").append(accounts.size()).append("):\n");
            for (Account a : accounts) {
                out.append(" - ").append(a.getName())
                        .append(" (acct:").append(a.getAccountNumber()).append(", card:")
                        .append(a.getCardNumber()).append(")\n");
            }

            outputArea.setText(out.toString());

            // clear inputs (optional)
            nameField.clear();
            phoneField.clear();
            depositField.clear();

        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            errorLabel.setText("Failed to create account: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
