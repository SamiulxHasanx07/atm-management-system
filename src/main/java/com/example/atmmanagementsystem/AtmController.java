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
        WELCOME, CARD_INPUT, PIN_INPUT, LOGGED_IN, DEPOSIT_INPUT, WITHDRAW_INPUT,
        FP_ENTER_CARD, FP_ENTER_IDENTITY
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
        } else if (currentMode == AtmMode.DEPOSIT_INPUT) {
            handleDepositInput(input);
        } else if (currentMode == AtmMode.WITHDRAW_INPUT) {
            handleWithdrawInput(input);
        } else if (currentMode == AtmMode.FP_ENTER_CARD) {
            handleForgotPinCardInput(input);
        } else if (currentMode == AtmMode.FP_ENTER_IDENTITY) {
            handleForgotPinIdentityInput(input);
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

    private void handleDepositInput(String input) {
        try {
            double amount = Double.parseDouble(input);
            service.deposit(currentCardNumber, amount);

            Account updatedAcc = service.findByCardNumber(currentCardNumber).orElse(null);
            String balanceStr = (updatedAcc != null) ? String.format("%.2f", updatedAcc.getBalance()) : "N/A";

            screenMessage.setText("Deposited " + String.format("%.0f", amount) + " TK. Balance: " + balanceStr + " TK");
            currentMode = AtmMode.LOGGED_IN;
            updateOptions();
        } catch (NumberFormatException e) {
            screenMessage.setText("Invalid amount. Enter numbers only.");
        } catch (IllegalArgumentException e) {
            screenMessage.setText(e.getMessage());
        } catch (Exception e) {
            screenMessage.setText("Error: " + e.getMessage());
        }
    }

    private void handleWithdrawInput(String input) {
        try {
            double amount = Double.parseDouble(input);
            service.withdraw(currentCardNumber, amount);

            Account updatedAcc = service.findByCardNumber(currentCardNumber).orElse(null);
            String balanceStr = (updatedAcc != null) ? String.format("%.2f", updatedAcc.getBalance()) : "N/A";

            screenMessage.setText("Withdrawn " + String.format("%.0f", amount) + " TK. Balance: " + balanceStr + " TK");
            currentMode = AtmMode.LOGGED_IN;
            updateOptions();
        } catch (NumberFormatException e) {
            screenMessage.setText("Invalid amount. Enter numbers only.");
        } catch (IllegalArgumentException e) {
            screenMessage.setText(e.getMessage());
        } catch (Exception e) {
            screenMessage.setText("Error: " + e.getMessage());
        }
    }

    private void handleForgotPinCardInput(String inputCard) {
        if (inputCard == null || inputCard.length() < 10) {
            screenMessage.setText("Invalid Card Number. Try again.");
            return;
        }
        // Valid length, check existence
        Account acc = service.findByCardNumber(inputCard).orElse(null);
        if (acc == null) {
            screenMessage.setText("Card not found. Please try again.");
            return;
        }

        // Card found, ask for identity
        currentCardNumber = inputCard; // repurpose this field or add temp one? Repurpose is fine for this flow.
        currentMode = AtmMode.FP_ENTER_IDENTITY;
        screenMessage.setText("Enter Registered Phone Number OR NID:");
        updateOptions();
    }

    private void handleForgotPinIdentityInput(String identity) {
        if (identity == null || identity.isEmpty()) {
            screenMessage.setText("Enter Phone or NID.");
            return;
        }

        try {
            String newPin = service.resetPin(currentCardNumber, identity);
            screenMessage.setText("Success! New PIN: " + newPin + ". Account Unblocked.");
            // Reset to welcome or allow login? "return to Welcome" is safer so they can
            // memorize PIN
            currentMode = AtmMode.WELCOME;
            currentCardNumber = null;
            updateOptions();
        } catch (IllegalArgumentException e) {
            screenMessage.setText(e.getMessage());
        } catch (Exception e) {
            screenMessage.setText("Error: " + e.getMessage());
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
            case DEPOSIT_INPUT:
            case WITHDRAW_INPUT:
            case FP_ENTER_CARD:
            case FP_ENTER_IDENTITY:
                // Usually buttons are disabled or have limited function (like Cancel/Exit)
                // We'll allow "Exit" or "Cancel" if mapped
                if (optionCode.equals("R4")) { // Assume R4 is Exit/Cancel roughly
                    // If in txn mode, go back to logged in? Or Eject?
                    if (currentMode == AtmMode.DEPOSIT_INPUT || currentMode == AtmMode.WITHDRAW_INPUT) {
                        currentMode = AtmMode.LOGGED_IN;
                        screenMessage.setText("Transaction Cancelled.");
                        updateOptions();
                    } else {
                        resetSession(); // Card/PIN/ForgotPIN input cancel -> Reset
                    }
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
            case "R1": // Forgot PIN
                enterForgotPinMode();
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
            case "L1": // Withdraw
                currentMode = AtmMode.WITHDRAW_INPUT;
                screenMessage.setText("Enter Amount (Multiple of 500):");
                updateOptions();
                break;
            case "L2":
                Account acc = service.findByCardNumber(currentCardNumber).orElse(null);
                if (acc != null) {
                    screenMessage
                            .setText("Your Current Balance is: " + String.format("%.2f", acc.getBalance()) + " TK");
                } else {
                    screenMessage.setText("Error retrieving balance.");
                }
                break;
            case "R1": // Deposit
                currentMode = AtmMode.DEPOSIT_INPUT;
                screenMessage.setText("Enter Amount (Multiple of 500):");
                updateOptions();
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

    private void enterForgotPinMode() {
        currentMode = AtmMode.FP_ENTER_CARD;
        screenMessage.setText("Enter Card Number to reset PIN.");
        updateOptions();
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
        } else if (currentMode == AtmMode.CARD_INPUT || currentMode == AtmMode.PIN_INPUT ||
                currentMode == AtmMode.DEPOSIT_INPUT || currentMode == AtmMode.WITHDRAW_INPUT ||
                currentMode == AtmMode.FP_ENTER_CARD || currentMode == AtmMode.FP_ENTER_IDENTITY) {
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
