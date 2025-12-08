package com.example.atmmanagementsystem;

import com.example.atmmanagementsystem.model.Account;
import com.example.atmmanagementsystem.service.AccountService;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import javafx.util.Duration;

public class AtmController {
    @FXML
    private TextField screenInput;

    @FXML
    private Label screenMessage;

    @FXML
    private HBox cardReaderSlot;

    @FXML
    private Label optionLeft1;
    @FXML
    private Label optionRight1;

    @FXML
    private Label optionLeft2;
    @FXML
    private Label optionRight2;

    @FXML
    private Label optionLeft3;
    @FXML
    private Label optionRight3;

    @FXML
    private Label optionLeft4;
    @FXML
    private Label optionRight4;

    private boolean cardInserted = false;
    private String currentCardNumber;

    private final AccountService service = AccountService.getInstance();

    @FXML
    public void initialize() {
        updateOptions();
    }

    @FXML
    private void onNum(javafx.event.ActionEvent ev) {
        String val = ((javafx.scene.control.Button) ev.getSource()).getText();
        if (screenInput != null)
            screenInput.appendText(val);
    }

    @FXML
    private void onClear() {
        if (screenInput != null)
            screenInput.clear();
    }

    @FXML
    private void onOk() {
        if (!cardInserted) {
            screenMessage.setText("Please insert card first.");
            return;
        }

        // card is inserted: treat input as PIN attempt
        String pin = screenInput.getText();
        if (pin == null || !pin.matches("\\d{4}")) {
            screenMessage.setText("Enter 4-digit PIN and press OK");
            return;
        }

        Account acc = service.findByCardNumber(currentCardNumber).orElse(null);
        if (acc == null) {
            screenMessage.setText("Card not recognized");
            return;
        }

        if (acc.getPin().equals(pin)) {
            screenMessage.setText("Login successful. Welcome " + acc.getName());
        } else {
            screenMessage.setText("Incorrect PIN");
        }
        screenInput.clear();
    }

    // Left Side Buttons
    @FXML
    private void onSideBtnLeft1() {
        handleOption("L1");
    }

    @FXML
    private void onSideBtnLeft2() {
        handleOption("L2");
    }

    @FXML
    private void onSideBtnLeft3() {
        handleOption("L3");
    }

    @FXML
    private void onSideBtnLeft4() {
        handleOption("L4");
    }

    // Right Side Buttons
    @FXML
    private void onSideBtnRight1() {
        handleOption("R1");
    }

    @FXML
    private void onSideBtnRight2() {
        handleOption("R2");
    }

    @FXML
    private void onSideBtnRight3() {
        handleOption("R3");
    }

    @FXML
    private void onSideBtnRight4() {
        handleOption("R4");
    }

    private void handleOption(String optionCode) {
        if (!cardInserted) {
            switch (optionCode) {
                case "L1": // Create Account
                    try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                                getClass().getResource("bangla-bank-view.fxml"));
                        javafx.scene.Parent root = loader.load();
                        BanglaBankController controller = loader.getController();
                        controller.showCreateAccount();

                        screenMessage.getScene().setRoot(root);
                    } catch (Exception e) {
                        e.printStackTrace();
                        screenMessage.setText("Error loading Create Account screen.");
                    }
                    break;
                case "R1": // Forgot PIN
                    screenMessage.setText("Feature 'Forgot PIN' not implemented yet.");
                    break;
                case "L2": // Insert Card
                    simulateCardInsertion();
                    break;
                case "R2": // Deposit Cash
                    screenMessage.setText("Feature 'Deposit Cash' not implemented yet.");
                    break;
                default:
                    break;
            }
        } else {
            // Options when card is inserted
            switch (optionCode) {
                case "L1": // Withdraw
                    screenMessage.setText("Withdraw feature not implemented.");
                    break;
                case "R1": // Deposit
                    screenMessage.setText("Deposit feature not implemented.");
                    break;
                case "L2": // Check Balance
                    screenMessage.setText("Balance check not implemented.");
                    break;
                case "R2": // Eject Card
                    ejectCard();
                    break;
                default:
                    break;
            }
        }
    }

    private void simulateCardInsertion() {
        // Simulate finding a card to insert
        if (!service.listAccounts().isEmpty()) {
            currentCardNumber = service.listAccounts().get(0).getCardNumber();
        } else {
            // Fallback if no accounts exist
            currentCardNumber = "123456789012";
        }

        cardInserted = true;
        animateCardReader();
        screenMessage.setText("Card inserted. Enter PIN and press OK.");
        updateOptions();
    }

    private void ejectCard() {
        cardInserted = false;
        currentCardNumber = null;
        screenMessage.setText("Card ejected. Please take your card.");
        updateOptions();
    }

    private void updateOptions() {
        if (optionLeft1 == null)
            return;

        // Clear all first
        optionLeft1.setText("");
        optionRight1.setText("");
        optionLeft2.setText("");
        optionRight2.setText("");
        optionLeft3.setText("");
        optionRight3.setText("");
        optionLeft4.setText("");
        optionRight4.setText("");

        if (cardInserted) {
            optionLeft1.setText("Withdraw");
            optionRight1.setText("Deposit");
            optionLeft2.setText("Check Balance");
            optionRight2.setText("Eject Card");
        } else {
            optionLeft1.setText("Create Account");
            optionRight1.setText("Forgot PIN");
            optionLeft2.setText("Insert Card");
            optionRight2.setText("Deposit Cash");
        }
    }

    private void animateCardReader() {
        if (cardReaderSlot == null)
            return;

        // Flash effect or slide effect
        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(cardReaderSlot.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(200), new KeyValue(cardReaderSlot.opacityProperty(), 0.3)),
                new KeyFrame(Duration.millis(400), new KeyValue(cardReaderSlot.opacityProperty(), 1.0)));
        t.setCycleCount(3);
        t.play();
    }
}
