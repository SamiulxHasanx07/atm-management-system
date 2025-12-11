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

    private enum AtmMode {
        WELCOME, CARD_INPUT, PIN_INPUT, LOGGED_IN
    }

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

    private AtmMode currentMode = AtmMode.WELCOME;
    private String currentCardNumber;
    private int failedAttempts = 0;

    private final AccountService service = AccountService.getInstance();

    @FXML
    public void initialize() {
        updateOptions();
    }

    @FXML
    private void onNum(javafx.event.ActionEvent ev) {
        // Only allow input in CARD or PIN modes
        if (currentMode == AtmMode.WELCOME || currentMode == AtmMode.LOGGED_IN)
            return;

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
        String input = screenInput.getText();

        if (currentMode == AtmMode.CARD_INPUT) {
            handleCardInput(input);
        } else if (currentMode == AtmMode.PIN_INPUT) {
            handlePinInput(input);
        } else if (currentMode == AtmMode.WELCOME) {
            screenMessage.setText("Please select an option.");
        }
        screenInput.clear();
    }

    private void handleCardInput(String inputCard) {
        if (inputCard == null || inputCard.length() < 10) { // Basic length check
            screenMessage.setText("Invalid Card Number. Try again.");
            return;
        }

        Account acc = service.findByCardNumber(inputCard).orElse(null);
        if (acc == null) {
            screenMessage.setText("Card not found. Please try again.");
            return;
        }

        if (acc.isBlocked()) {
            screenMessage.setText("This card is BLOCKED. Please contact bank.");
            // Reset to welcome after delay or immediate?
            // For now, let them see the message, they can press Exit or Eject (if we had
            // one in this state)
            // But strict requirement: "redirect main menu" on eject.
            // Let's just create a temporary state or stay in CARD_INPUT but show error?
            // Safest: Go back to Welcome? Or just reset input.
            currentMode = AtmMode.WELCOME;
            updateOptions();
            return;
        }

        // Valid card
        currentCardNumber = inputCard;
        currentMode = AtmMode.PIN_INPUT;
        failedAttempts = 0; // Reset attempts for new user
        screenMessage.setText("Card Accepted. Enter PIN:");
        updateOptions();
    }

    private void handlePinInput(String inputPin) {
        if (inputPin == null || !inputPin.matches("\\d{4}")) {
            screenMessage.setText("Invalid PIN format. Enter 4 digits.");
            return;
        }

        Account acc = service.findByCardNumber(currentCardNumber).orElse(null);
        // Should exist as we checked in CARD_INPUT, unless deleted concurrently
        if (acc == null) {
            resetSession();
            screenMessage.setText("System Error: Card not found.");
            return;
        }

        String hashedInputPin = com.example.atmmanagementsystem.util.SecurityUtil.hashPin(inputPin);
        if (acc.getPin().equals(hashedInputPin)) {
            // Success
            currentMode = AtmMode.LOGGED_IN;
            failedAttempts = 0;
            screenMessage.setText("Welcome Back, " + acc.getName());
            updateOptions();
        } else {
            // Fail
            failedAttempts++;
            if (failedAttempts >= 3) {
                service.blockAccount(currentCardNumber);
                screenMessage.setText("Wrong PIN 3 times. CARD BLOCKED.");
                resetSession();
            } else {
                screenMessage.setText("Incorrect PIN. Attempt " + failedAttempts + "/3");
            }
        }
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
        switch (currentMode) {
            case WELCOME:
                handleWelcomeOptions(optionCode);
                break;
            case CARD_INPUT:
            case PIN_INPUT:
                // Usually buttons are disabled or have limited function (like Cancel/Exit)
                // We'll allow "Exit" or "Cancel" if mapped
                if (optionCode.equals("R4")) { // Assume R4 is Exit/Cancel roughly
                    resetSession();
                }
                break;
            case LOGGED_IN:
                handleLoggedInOptions(optionCode);
                break;
        }
    }

    private void handleWelcomeOptions(String code) {
        switch (code) {
            case "L1": // Create Account
                loadCreateAccount();
                break;
            case "L2": // Insert Card
                enterCardInputMode();
                break;
            case "R2": // Exit
                javafx.application.Platform.exit();
                break;
            default:
                screenMessage.setText("Please insert card to continue.");
        }
    }

    private void handleLoggedInOptions(String code) {
        switch (code) {
            case "L1":
                screenMessage.setText("Withdraw feature coming soon.");
                break;
            case "L2":
                screenMessage.setText("Check Balance feature coming soon.");
                break;
            case "R1":
                screenMessage.setText("Deposit feature coming soon.");
                break;
            case "R2": // Eject Card
                ejectCard();
                break;
        }
    }

    private void loadCreateAccount() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("create-account.fxml"));
            javafx.scene.Parent root = loader.load();
            BanglaBankController controller = loader.getController();
            controller.showCreateAccount();
            screenMessage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            screenMessage.setText("Error loading Create Account screen.");
        }
    }

    private void enterCardInputMode() {
        currentMode = AtmMode.CARD_INPUT;
        screenMessage.setText("Please enter your Card Number using the keypad.");
        updateOptions();
        animateCardReader();
    }

    private void resetSession() {
        currentCardNumber = null;
        failedAttempts = 0;
        currentMode = AtmMode.WELCOME;
        updateOptions();
    }

    private void ejectCard() {
        screenMessage.setText("Card Ejected. Thank you.");
        resetSession();
    }

    private void updateOptions() {
        if (optionLeft1 == null)
            return;

        // Clear all
        optionLeft1.setText("");
        optionRight1.setText("");
        optionLeft2.setText("");
        optionRight2.setText("");
        optionLeft3.setText("");
        optionRight3.setText("");
        optionLeft4.setText("");
        optionRight4.setText("");

        if (currentMode == AtmMode.WELCOME) {
            optionLeft1.setText("Create Account");
            optionLeft2.setText("Insert Card");
            optionRight1.setText("Forgot PIN");
            optionRight2.setText("Exit");
        } else if (currentMode == AtmMode.LOGGED_IN) {
            optionLeft1.setText("Withdraw");
            optionRight1.setText("Deposit");
            optionLeft2.setText("Check Balance");
            optionRight2.setText("Eject Card");
        } else if (currentMode == AtmMode.CARD_INPUT) {
            optionRight4.setText("Cancel");
        } else if (currentMode == AtmMode.PIN_INPUT) {
            optionRight4.setText("Cancel");
        }
    }

    private void animateCardReader() {
        if (cardReaderSlot == null)
            return;
        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(cardReaderSlot.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(200), new KeyValue(cardReaderSlot.opacityProperty(), 0.3)),
                new KeyFrame(Duration.millis(400), new KeyValue(cardReaderSlot.opacityProperty(), 1.0)));
        t.setCycleCount(3);
        t.play();
    }
}
