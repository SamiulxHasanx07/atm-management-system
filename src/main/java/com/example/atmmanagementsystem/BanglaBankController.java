package com.example.atmmanagementsystem;

import com.example.atmmanagementsystem.model.Account;
import com.example.atmmanagementsystem.service.AccountService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class BanglaBankController {

    @FXML
    private VBox createPane;

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
    private Label resultTitle;

    @FXML
    private Label createResultLabel;

    @FXML
    private VBox resultContainer;

    private final AccountService service = new AccountService();

    @FXML
    public void initialize() {
        showPane(createPane);
    }

    public void showCreateAccount() {
        showPane(createPane);
    }

    private void showPane(VBox pane) {

        errorLabel.setText("");
        resultTitle.setText("Result");
        createResultLabel.setText("");
        if (resultContainer != null) {
            resultContainer.setVisible(false);
            resultContainer.setManaged(false);
        }
    }

    @FXML
    protected void onCreateAccount() {
        errorLabel.setText("");
        outputArea.clear();
        createResultLabel.setText("");
        resultContainer.setVisible(false);
        resultContainer.setManaged(false);
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
            resultTitle.setText("Successfully Created Account");
            resultContainer.setVisible(true);
            resultContainer.setManaged(true);
            StringBuilder out = new StringBuilder();
            out.append("Name: ").append(acc.getName()).append("\n");
            out.append("Account#: ").append(acc.getAccountNumber()).append("\n");
            out.append("Card#:    ").append(acc.getCardNumber()).append("\n");
            out.append("PIN:      ").append(acc.getPin()).append("\n");
            out.append("Balance:  ").append(String.format("%.2f", acc.getBalance())).append("\n\n");
            outputArea.setText(out.toString());
            createResultLabel.setText("New card: " + acc.getCardNumber() + "   PIN: " + acc.getPin());
            // after creation, prefill card input so user can login immediately

        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            errorLabel.setText("Failed to create account: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void onBackToWelcome() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("welcome-screen.fxml"));
            javafx.scene.Parent root = loader.load();
            nameField.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error returning to Main Menu");
        }
    }

    @FXML
    protected void onCopyInfo() {
        String info = outputArea.getText();
        if (info != null && !info.isEmpty()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(info);
            clipboard.setContent(content);
            createResultLabel.setText("Copied to Clipboard!");
        }
    }
}
