package com.example.atmmanagementsystem;

import com.example.atmmanagementsystem.model.Account;
import com.example.atmmanagementsystem.service.AccountService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class BanglaBankController {
    @FXML
    private VBox welcomePane;

    @FXML
    private VBox loginPane;

    @FXML
    private VBox createPane;

    @FXML
    private TextField cardInput;

    @FXML
    private Label welcomeError;

    @FXML
    private Label cardLabel;

    @FXML
    private PasswordField pinField;

    @FXML
    private Label loginError;

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

    @FXML
    private Label createResultLabel;

    private final AccountService service = new AccountService();
    private String currentCardNumber; // for login flow

    @FXML
    public void initialize() {
        showPane(welcomePane);
    }

    private void showPane(VBox pane) {
        welcomePane.setVisible(false);
        welcomePane.setManaged(false);
        loginPane.setVisible(false);
        loginPane.setManaged(false);
        createPane.setVisible(false);
        createPane.setManaged(false);

        pane.setVisible(true);
        pane.setManaged(true);
        // clear messages
        welcomeError.setText("");
        loginError.setText("");
        errorLabel.setText("");
        createResultLabel.setText("");
    }

    @FXML
    protected void onInsertCard() {
        welcomeError.setText("");
        String card = cardInput.getText();
        if (card == null) {
            welcomeError.setText("Please enter a card number");
            return;
        }
        String digits = card.replaceAll("\\D", "");
        if (digits.length() < 12) {
            welcomeError.setText("Card number looks invalid");
            return;
        }

        Optional<Account> maybe = service.findByCardNumber(digits);
        if (maybe.isPresent()) {
            currentCardNumber = digits;
            cardLabel.setText(maskCard(digits));
            pinField.clear();
            showPane(loginPane);
        } else {
            welcomeError.setText("Card not recognized. New user? Click 'Apply card' to create an account.");
        }
    }

    @FXML
    protected void onApplyCard() {
        // go to create account pane
        showPane(createPane);
    }

    @FXML
    protected void onCancelLogin() {
        cardInput.clear();
        showPane(welcomePane);
    }

    @FXML
    protected void onLogin() {
        loginError.setText("");
        if (currentCardNumber == null) {
            loginError.setText("No card selected");
            return;
        }
        String pin = pinField.getText();
        if (pin == null || !pin.matches("\\d{4}")) {
            loginError.setText("PIN must be 4 digits");
            return;
        }

        Optional<Account> maybe = service.findByCardNumber(currentCardNumber);
        if (maybe.isEmpty()) {
            loginError.setText("Card not found");
            return;
        }

        Account acc = maybe.get();
        if (acc.getPin().equals(pin)) {
            // success: show account summary in output area
            StringBuilder out = new StringBuilder();
            out.append("Login successful:\n");
            out.append("Name: ").append(acc.getName()).append("\n");
            out.append("Account#: ").append(acc.getAccountNumber()).append("\n");
            out.append("Card#:    ").append(acc.getCardNumber()).append("\n");
            out.append("Balance:  ").append(String.format("%.2f", acc.getBalance())).append("\n");
            outputArea.setText(out.toString());
        } else {
            loginError.setText("Incorrect PIN");
        }
    }

    @FXML
    protected void onBackToWelcome() {
        // clear create form
        nameField.clear();
        phoneField.clear();
        depositField.clear();
        showPane(welcomePane);
    }

    @FXML
    protected void onCreateAccount() {
        errorLabel.setText("");
        String name = nameField.getText();
        String phone = phoneField.getText();
        String depositText = depositField.getText();

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

            List<Account> accounts = service.listAccounts();
            out.append("Stored accounts (count=").append(accounts.size()).append("):\n");
            for (Account a : accounts) {
                out.append(" - ").append(a.getName())
                        .append(" (acct:").append(a.getAccountNumber()).append(", card:")
                        .append(a.getCardNumber()).append(")\n");
            }

            outputArea.setText(out.toString());
            createResultLabel.setText("New card: " + acc.getCardNumber() + "   PIN: " + acc.getPin());

            // after creation, prefill card input so user can login immediately
            cardInput.setText(acc.getCardNumber());
            showPane(welcomePane);
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            errorLabel.setText("Failed to create account: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String maskCard(String card) {
        if (card == null) return "";
        String digits = card.replaceAll("\\s+", "");
        if (digits.length() <= 4) return digits;
        return "**** **** **** " + digits.substring(digits.length() - 4);
    }
}
