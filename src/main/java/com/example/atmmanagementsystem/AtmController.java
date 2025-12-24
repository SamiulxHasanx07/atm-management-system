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
        FP_ENTER_CARD, FP_ENTER_NID, FP_NEW_PIN, FP_CONFIRM_PIN,
        CHANGE_PIN_INPUT, CHANGE_PIN_CONFIRM,
        DISABLE_ENTER_CARD, DISABLE_ENTER_NID
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
    private String tempNewPin;
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
        } else if (currentMode == AtmMode.FP_ENTER_CARD) {
            handleForgotPinCardInput(input);
        } else if (currentMode == AtmMode.FP_ENTER_NID) {
            handleForgotPinNidInput(input);
        } else if (currentMode == AtmMode.FP_NEW_PIN) {
            handleForgotPinNewPinInput(input);
        } else if (currentMode == AtmMode.FP_CONFIRM_PIN) {
            handleForgotPinConfirmInput(input);
        } else if (currentMode == AtmMode.CHANGE_PIN_INPUT) {
            handleChangePinNewInput(input);
        } else if (currentMode == AtmMode.CHANGE_PIN_CONFIRM) {
            handleChangePinConfirmInput(input);
        } else if (currentMode == AtmMode.DISABLE_ENTER_CARD) {
            handleDisableEnterCard(input);
        } else if (currentMode == AtmMode.DISABLE_ENTER_NID) {
            handleDisableEnterNid(input);
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
        currentCardNumber = inputCard;
        currentMode = AtmMode.FP_ENTER_NID;

        String nid = acc.getNid();
        String maskedNid = "****";
        if (nid != null && nid.length() > 4) {
            maskedNid = nid.substring(0, nid.length() - 4) + "****";
        }

        screenMessage.setText("Enter Last 4 Digits of NID " + maskedNid + ":");
        updateOptions();
    }

    private void handleForgotPinNidInput(String input) {
        if (input == null || !input.matches("\\d{4}")) {
            screenMessage.setText("Enter exactly 4 digits of NID.");
            return;
        }

        Account acc = service.findByCardNumber(currentCardNumber).orElse(null);
        if (acc == null) {
            resetSession();
            screenMessage.setText("System Error: Card not found.");
            return;
        }

        String fullNid = acc.getNid();
        if (fullNid == null || fullNid.length() < 4) {
            screenMessage.setText("NID data invalid. Contact Bank.");
            return;
        }

        String last4 = fullNid.substring(fullNid.length() - 4);
        if (input.equals(last4)) {
            // Success match
            currentMode = AtmMode.FP_NEW_PIN;
            screenMessage.setText("Verified! Enter New PIN (4 digits):");
            updateOptions();
        } else {
            screenMessage.setText("Incorrect NID digits. Try again.");
        }
    }

    private void handleForgotPinNewPinInput(String newPin) {
        if (newPin == null || !newPin.matches("\\d{4}")) {
            screenMessage.setText("Invalid PIN format. Enter 4 digits.");
            return;
        }
        tempNewPin = newPin;
        currentMode = AtmMode.FP_CONFIRM_PIN;
        screenMessage.setText("Confirm New PIN:");
        updateOptions();
    }

    private void handleForgotPinConfirmInput(String confirmPin) {
        if (confirmPin == null || !confirmPin.equals(tempNewPin)) {
            screenMessage.setText("PIN mismatch. Start over.");
            currentMode = AtmMode.FP_NEW_PIN;
            screenMessage.setText("Enter New PIN (4 digits):");
            tempNewPin = null;
            updateOptions();
            return;
        }

        try {
            // Update PIN directly (check against old PIN not strictly required here as they
            // forgot it,
            // but updatePin might enforce it if using the same service method.
            // The service method throws if newPin == oldPin.
            // If user enters SAME pin as forgot pin, it will error. This is acceptable?
            // Yes, "New PIN cannot be same as old PIN" is a valid security rule even if
            // forgot.
            service.updatePin(currentCardNumber, confirmPin);

            // Unblock if blocked
            // Note: updatePin does NOT unblock automatically in our previous implementation
            // of updatePin
            // But resetPin DID. We probably should unblock here.
            // Let's create a new service method or add unblock logic?
            // OR just call resetPin logic but providing the new PIN?
            // Actually, updatePin in DatabaseAccountService does: "UPDATE accounts SET pin
            // = ? ..."
            // It does NOT unblock.
            // We need to unblock.
            service.unblockAccount(currentCardNumber); // We need to add this method or direct DB call?
            // Or update updatePin to unblock?
            // Let's rely on updatePin for now, and realized we need to unblock.

            screenMessage.setText("PIN Reset Success! Please Login.");
            resetSession();
        } catch (IllegalArgumentException e) {
            screenMessage.setText(e.getMessage());
            currentMode = AtmMode.FP_NEW_PIN;
            updateOptions();
        } catch (Exception e) {
            screenMessage.setText("Error: " + e.getMessage());
        }
    }

    private void handleChangePinNewInput(String newPin) {
        if (newPin == null || !newPin.matches("\\d{4}")) {
            screenMessage.setText("Invalid PIN format. Enter 4 digits.");
            return;
        }

        // Save first input and ask for confirmation
        tempNewPin = newPin;
        currentMode = AtmMode.CHANGE_PIN_CONFIRM;
        screenMessage.setText("Please Re-enter PIN to Confirm:");
        updateOptions();
    }

    private void handleChangePinConfirmInput(String confirmPin) {
        if (confirmPin == null || !confirmPin.equals(tempNewPin)) {
            screenMessage.setText("PIN mismatch. Please start over.");
            currentMode = AtmMode.CHANGE_PIN_INPUT;
            screenMessage.setText("Enter New PIN (4 digits):");
            tempNewPin = null;
            updateOptions();
            return;
        }

        try {
            service.updatePin(currentCardNumber, confirmPin);
            screenMessage.setText("PIN Changed Successfully.");
            currentMode = AtmMode.LOGGED_IN;
            tempNewPin = null;
            updateOptions();
        } catch (IllegalArgumentException e) {
            screenMessage.setText(e.getMessage());
            currentMode = AtmMode.CHANGE_PIN_INPUT; // Let them try again
            updateOptions();
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

    private void handleDisableEnterCard(String inputCard) {
        if (inputCard == null || inputCard.length() < 10) {
            screenMessage.setText("Invalid Card Number. Try again.");
            return;
        }
        Account acc = service.findByCardNumber(inputCard).orElse(null);
        if (acc == null) {
            screenMessage.setText("Card not found. Please try again.");
            return;
        }
        currentCardNumber = inputCard;
        currentMode = AtmMode.DISABLE_ENTER_NID;

        String nid = acc.getNid();
        String maskedNid = "****";
        if (nid != null && nid.length() > 4) {
            maskedNid = nid.substring(0, nid.length() - 4) + "****";
        }

        screenMessage.setText("Enter Last 4 Digits of NID " + maskedNid + " to BLOCK:");
        updateOptions();
    }

    private void handleDisableEnterNid(String input) {
        if (input == null || !input.matches("\\d{4}")) {
            screenMessage.setText("Enter exactly 4 digits of NID.");
            return;
        }
        Account acc = service.findByCardNumber(currentCardNumber).orElse(null);
        if (acc == null) {
            resetSession(); // Should not happen usually
            return;
        }

        String fullNid = acc.getNid();
        if (fullNid == null || fullNid.length() < 4) {
            screenMessage.setText("NID data invalid. Contact Bank.");
            return;
        }

        String last4 = fullNid.substring(fullNid.length() - 4);
        if (input.equals(last4)) {
            // Match - Block Card
            try {
                service.blockAccount(currentCardNumber);
                screenMessage.setText("Card Successfully BLOCKED. Please contact bank.");
                resetSession();
            } catch (Exception e) {
                screenMessage.setText("Error blocking card: " + e.getMessage());
            }
        } else {
            screenMessage.setText("Incorrect NID digits. Try again.");
        }
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
            case FP_ENTER_NID:
            case FP_NEW_PIN:
            case FP_CONFIRM_PIN:
            case DISABLE_ENTER_CARD:
            case DISABLE_ENTER_NID:
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
            case CHANGE_PIN_INPUT:
            case CHANGE_PIN_CONFIRM:
                if (optionCode.equals("R4")) {
                    currentMode = AtmMode.LOGGED_IN;
                    screenMessage.setText("Change PIN Cancelled.");
                    tempNewPin = null;
                    updateOptions();
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
            case "L3": // Lost/Disable
                enterDisableCardMode();
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
            case "L3": // Change PIN
                currentMode = AtmMode.CHANGE_PIN_INPUT;
                tempNewPin = null;
                screenMessage.setText("Enter New PIN (4 digits):");
                updateOptions();
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

    private void enterDisableCardMode() {
        currentMode = AtmMode.DISABLE_ENTER_CARD;
        screenMessage.setText("Enter Card Number to Block/Disable:");
        updateOptions();
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
            optionLeft3.setText("Card Lost?");
            optionRight1.setText("Forgot PIN");
            optionRight2.setText("Exit");
        } else if (currentMode == AtmMode.LOGGED_IN) {
            optionLeft1.setText("Withdraw");
            optionRight1.setText("Deposit");
            optionLeft2.setText("Check Balance");
            optionRight2.setText("Eject Card");
            optionLeft3.setText("Change PIN");
        } else if (currentMode == AtmMode.CARD_INPUT || currentMode == AtmMode.PIN_INPUT ||
                currentMode == AtmMode.DEPOSIT_INPUT || currentMode == AtmMode.WITHDRAW_INPUT ||
                currentMode == AtmMode.FP_ENTER_CARD || currentMode == AtmMode.FP_ENTER_NID ||
                currentMode == AtmMode.FP_NEW_PIN || currentMode == AtmMode.FP_CONFIRM_PIN ||
                currentMode == AtmMode.CHANGE_PIN_INPUT || currentMode == AtmMode.CHANGE_PIN_CONFIRM ||
                currentMode == AtmMode.DISABLE_ENTER_CARD || currentMode == AtmMode.DISABLE_ENTER_NID) {
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
