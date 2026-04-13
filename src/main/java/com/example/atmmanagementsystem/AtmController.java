package com.example.atmmanagementsystem;

import com.example.atmmanagementsystem.api.SessionManager;
import com.example.atmmanagementsystem.api.dto.LoginResponse;
import com.example.atmmanagementsystem.model.Account;
import com.example.atmmanagementsystem.service.AccountService;
import com.example.atmmanagementsystem.service.ApiAccountService;
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
        DISABLE_ENTER_CARD, DISABLE_ENTER_NID,
        DEPOSIT_NO_CARD_ACCOUNT, DEPOSIT_NO_CARD_NID, DEPOSIT_NO_CARD_AMOUNT
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
    private String tempNidProof; // Store NID proof for reset/block operations
    private String cardlessDepositAccountNumber; // Store account number for cardless deposit
    private String cardlessDepositNidProof; // Store NID proof for cardless deposit
    private int failedAttempts = 0;

    private final ApiAccountService apiService = (ApiAccountService) AccountService.getInstance();

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
        System.out.println("=== onOk() CALLED ===");
        String input = screenInput.getText();
        System.out.println("Current Mode: " + currentMode);
        System.out.println("Input: " + input);

        if (currentMode == AtmMode.CARD_INPUT) {
            System.out.println("Routing to handleCardInput");
            handleCardInput(input);
        } else if (currentMode == AtmMode.PIN_INPUT) {
            System.out.println("Routing to handlePinInput");
            handlePinInput(input);
        } else if (currentMode == AtmMode.DEPOSIT_INPUT) {
            System.out.println("Routing to handleDepositInput");
            handleDepositInput(input);
        } else if (currentMode == AtmMode.WITHDRAW_INPUT) {
            System.out.println("Routing to handleWithdrawInput");
            handleWithdrawInput(input);
        } else if (currentMode == AtmMode.FP_ENTER_CARD) {
            System.out.println("Routing to handleForgotPinCardInput");
            handleForgotPinCardInput(input);
        } else if (currentMode == AtmMode.FP_ENTER_CARD) {
            System.out.println("Routing to handleForgotPinCardInput");
            handleForgotPinCardInput(input);
        } else if (currentMode == AtmMode.FP_ENTER_NID) {
            System.out.println("Routing to handleForgotPinNidInput");
            handleForgotPinNidInput(input);
        } else if (currentMode == AtmMode.FP_NEW_PIN) {
            System.out.println("Routing to handleForgotPinNewPinInput");
            handleForgotPinNewPinInput(input);
        } else if (currentMode == AtmMode.FP_CONFIRM_PIN) {
            System.out.println("Routing to handleForgotPinConfirmInput");
            handleForgotPinConfirmInput(input);
        } else if (currentMode == AtmMode.CHANGE_PIN_INPUT) {
            System.out.println("Routing to handleChangePinNewInput");
            handleChangePinNewInput(input);
        } else if (currentMode == AtmMode.CHANGE_PIN_CONFIRM) {
            System.out.println("Routing to handleChangePinConfirmInput");
            handleChangePinConfirmInput(input);
        } else if (currentMode == AtmMode.DISABLE_ENTER_CARD) {
            System.out.println("Routing to handleDisableEnterCard");
            handleDisableEnterCard(input);
        } else if (currentMode == AtmMode.DISABLE_ENTER_NID) {
            System.out.println("Routing to handleDisableEnterNid");
            handleDisableEnterNid(input);
        } else if (currentMode == AtmMode.DEPOSIT_NO_CARD_ACCOUNT) {
            System.out.println("Routing to handleCardlessDepositAccountInput");
            handleCardlessDepositAccountInput(input);
        } else if (currentMode == AtmMode.DEPOSIT_NO_CARD_NID) {
            System.out.println("Routing to handleCardlessDepositNidInput");
            handleCardlessDepositNidInput(input);
        } else if (currentMode == AtmMode.DEPOSIT_NO_CARD_AMOUNT) {
            System.out.println("Routing to handleCardlessDepositAmountInput");
            handleCardlessDepositAmountInput(input);
        } else if (currentMode == AtmMode.WELCOME) {
            System.out.println("Routing to WELCOME handler");
            screenMessage.setText("Please select an option.");
        } else {
            System.out.println("WARNING: No handler for mode: " + currentMode);
        }
        screenInput.clear();
    }

    private void handleCardInput(String inputCard) {
        if (inputCard == null || inputCard.length() < 10) { // Basic length check
            screenMessage.setText("Invalid Card Number. Try again.");
            return;
        }

        // Just validate format and store - actual validation happens during login
        // The login endpoint will validate both card and PIN together
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

        try {
            // Use API login instead of checking hashed PIN directly
            LoginResponse loginResponse = apiService.login(currentCardNumber, inputPin);

            if (loginResponse.isSuccess() && loginResponse.getToken() != null) {
                // Success - token and user info are already stored in SessionManager by ApiService
                currentMode = AtmMode.LOGGED_IN;
                failedAttempts = 0;

                // Get username from session
                String userName = SessionManager.getInstance().getName();
                if (userName == null || userName.isEmpty()) {
                    userName = "User";
                }
                screenMessage.setText("Welcome, " + userName);
                updateOptions();
            } else {
                // Login failed - show error message from API
                String errorMsg = loginResponse.getMessage();
                if (errorMsg == null || errorMsg.isEmpty()) {
                    errorMsg = "Login failed";
                }

                failedAttempts++;
                if (failedAttempts >= 3) {
                    // Backend automatically blocks account after 3 failed attempts
                    screenMessage.setText("Wrong PIN 3 times. CARD BLOCKED. Contact bank.");
                    resetSession();
                } else {
                    screenMessage.setText(errorMsg + " (Attempt " + failedAttempts + "/3)");
                }
            }
        } catch (Exception e) {
            // Show the actual error message from the API
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = "Login failed. Please try again.";
            }

            failedAttempts++;
            if (failedAttempts >= 3) {
                // Backend automatically blocks account after 3 failed attempts
                screenMessage.setText("Wrong PIN 3 times. CARD BLOCKED. Contact bank.");
                resetSession();
            } else {
                screenMessage.setText(errorMsg + " (Attempt " + failedAttempts + "/3)");
            }
        }
    }

    private void handleDepositInput(String input) {
        try {
            System.out.println("=== DEPOSIT START ===");
            System.out.println("Current Card Number: " + currentCardNumber);
            System.out.println("Session: " + SessionManager.getInstance());
            System.out.println("Amount: " + input);
            
            double amount = Double.parseDouble(input);
            apiService.deposit(currentCardNumber, amount);
            
            // Refresh balance from API (reads from database)
            ((ApiAccountService) apiService).refreshSessionBalance(currentCardNumber);

            String balanceStr = String.format("%.2f", SessionManager.getInstance().getBalance());
            System.out.println("Deposit successful. New balance from API: " + balanceStr);
            screenMessage.setText("Deposited " + String.format("%.0f", amount) + " TK. Balance: " + balanceStr + " TK");
            currentMode = AtmMode.LOGGED_IN;
            updateOptions();
        } catch (NumberFormatException e) {
            screenMessage.setText("Invalid amount. Enter numbers only.");
        } catch (IllegalArgumentException e) {
            screenMessage.setText(e.getMessage());
            System.err.println("Deposit error: " + e.getMessage());
        } catch (Exception e) {
            screenMessage.setText("Deposit Error: " + e.getMessage());
            System.err.println("Deposit exception:");
            e.printStackTrace();
        }
    }

    private void handleWithdrawInput(String input) {
        try {
            System.out.println("=== WITHDRAW START ===");
            System.out.println("Current Card Number: " + currentCardNumber);
            System.out.println("Session: " + SessionManager.getInstance());
            System.out.println("Amount: " + input);
            
            double amount = Double.parseDouble(input);
            apiService.withdraw(currentCardNumber, amount);
            
            // Refresh balance from API (reads from database)
            ((ApiAccountService) apiService).refreshSessionBalance(currentCardNumber);

            String balanceStr = String.format("%.2f", SessionManager.getInstance().getBalance());
            System.out.println("Withdraw successful. New balance from API: " + balanceStr);
            screenMessage.setText("Withdrawn " + String.format("%.0f", amount) + " TK. Balance: " + balanceStr + " TK");
            currentMode = AtmMode.LOGGED_IN;
            updateOptions();
        } catch (NumberFormatException e) {
            screenMessage.setText("Invalid amount. Enter numbers only.");
        } catch (IllegalArgumentException e) {
            screenMessage.setText(e.getMessage());
            System.err.println("Withdraw error: " + e.getMessage());
        } catch (Exception e) {
            screenMessage.setText("Withdraw Error: " + e.getMessage());
            System.err.println("Withdraw exception:");
            e.printStackTrace();
        }
    }

    private void handleForgotPinCardInput(String inputCard) {
        if (inputCard == null || inputCard.length() < 10) {
            screenMessage.setText("Invalid Card Number. Try again.");
            return;
        }
        // Valid length, check existence
        Account acc = apiService.findByCardNumber(inputCard).orElse(null);
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

        Account acc = apiService.findByCardNumber(currentCardNumber).orElse(null);
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
            // Success match - store NID for later use
            tempNidProof = input;
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
            // Use resetPin which doesn't require auth and uses NID verification
            apiService.resetPin(currentCardNumber, tempNidProof);

            screenMessage.setText("PIN Reset Success! Please Login.");
            tempNewPin = null;
            tempNidProof = null;
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
            apiService.updatePin(currentCardNumber, confirmPin);
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
        Account acc = apiService.findByCardNumber(inputCard).orElse(null);
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
        Account acc = apiService.findByCardNumber(currentCardNumber).orElse(null);
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
                apiService.blockAccount(currentCardNumber, input);
                screenMessage.setText("Card Successfully BLOCKED. Please contact bank.");
                resetSession();
            } catch (Exception e) {
                screenMessage.setText("Error blocking card: " + e.getMessage());
            }
        } else {
            screenMessage.setText("Incorrect NID digits. Try again.");
        }
    }

    private void handleCardlessDepositAccountInput(String accountNum) {
        if (accountNum == null || accountNum.length() != 12) {
            screenMessage.setText("Invalid Account Number. Must be 12 digits.");
            return;
        }
        // Store account number and proceed to NID verification
        cardlessDepositAccountNumber = accountNum;
        currentMode = AtmMode.DEPOSIT_NO_CARD_NID;
        screenMessage.setText("Enter Last 4 Digits of NID:");
        updateOptions();
    }

    private void handleCardlessDepositNidInput(String input) {
        if (input == null || !input.matches("\\d{4}")) {
            screenMessage.setText("Enter exactly 4 digits.");
            return;
        }
        
        try {
            System.out.println("=== VERIFYING NID ===");
            System.out.println("Account Number: " + cardlessDepositAccountNumber);
            System.out.println("NID Proof: " + input);
            
            // Call verify NID API
            boolean verified = apiService.verifyNid(cardlessDepositAccountNumber, input);
            
            if (verified) {
                System.out.println("NID verification successful");
                // Store NID proof and proceed to amount entry
                cardlessDepositNidProof = input;
                currentMode = AtmMode.DEPOSIT_NO_CARD_AMOUNT;
                screenMessage.setText("NID Verified! Enter Amount to Deposit (Multiple of 500):");
                updateOptions();
            } else {
                screenMessage.setText("NID verification failed. Please try again.");
            }
        } catch (Exception e) {
            System.err.println("NID verification error: " + e.getMessage());
            screenMessage.setText("NID Error: " + e.getMessage());
        }
    }

    private void handleCardlessDepositAmountInput(String input) {
        System.out.println("=== handleCardlessDepositAmountInput CALLED ===");
        System.out.println("Input: " + input);
        System.out.println("Account Number: " + cardlessDepositAccountNumber);
        System.out.println("NID Proof: " + cardlessDepositNidProof);
        
        try {
            double amount = Double.parseDouble(input);
            
            System.out.println("=== CARDLESS DEPOSIT ===");
            System.out.println("Account Number: " + cardlessDepositAccountNumber);
            System.out.println("NID Proof: " + cardlessDepositNidProof);
            System.out.println("Amount: " + amount);
            
            if (cardlessDepositAccountNumber == null || cardlessDepositAccountNumber.isEmpty()) {
                screenMessage.setText("Error: Account number not found. Please start over.");
                resetSession();
                return;
            }
            
            if (cardlessDepositNidProof == null || cardlessDepositNidProof.isEmpty()) {
                screenMessage.setText("Error: NID verification failed. Please start over.");
                resetSession();
                return;
            }
            
            // Send cardless deposit request with all 3 pieces of data
            apiService.cardlessDeposit(cardlessDepositAccountNumber, cardlessDepositNidProof, amount);
            
            System.out.println("Cardless deposit successful");
            
            // Clear cardless deposit data
            cardlessDepositAccountNumber = null;
            cardlessDepositNidProof = null;
            
            // Store message before reset
            String successMsg = "Success! Deposited " + String.format("%.0f", amount) + " TK.";
            
            resetSession();
            
            // Set message after reset so it's visible
            screenMessage.setText(successMsg);
        } catch (NumberFormatException e) {
            screenMessage.setText("Invalid amount. Enter numbers only.");
            System.err.println("Cardless deposit amount parse error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            screenMessage.setText(e.getMessage());
            System.err.println("Cardless deposit validation error: " + e.getMessage());
        } catch (Exception e) {
            screenMessage.setText("Cardless Deposit Error: " + e.getMessage());
            System.err.println("Cardless deposit exception:");
            e.printStackTrace();
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
            case DEPOSIT_NO_CARD_ACCOUNT:
            case DEPOSIT_NO_CARD_NID:
            case DEPOSIT_NO_CARD_AMOUNT:
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
            case "L4": // Cardless Deposit
                enterCardlessDepositMode();
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
            case "L2": // Check Balance
                // Use session balance
                double balance = SessionManager.getInstance().getBalance();
                screenMessage.setText("Your Current Balance is: " + String.format("%.2f", balance) + " TK");
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
            case "R3": // Mini Statement
                loadMiniStatement();
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

    private void loadMiniStatement() {
        try {
            System.out.println("=== Loading Mini Statement ===");
            System.out.println("Current Card Number: " + currentCardNumber);
            System.out.println("Session: " + SessionManager.getInstance());
            
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("mini-statement.fxml"));
            javafx.scene.Parent root = loader.load();
            MiniStatementController controller = loader.getController();
            controller.setCardNumber(currentCardNumber);
            screenMessage.getScene().setRoot(root);
        } catch (Exception e) {
            System.err.println("Error loading Mini Statement screen: " + e.getMessage());
            e.printStackTrace();
            screenMessage.setText("Error loading Mini Statement: " + e.getMessage());
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
        tempNewPin = null;
        tempNidProof = null;
        cardlessDepositAccountNumber = null;
        cardlessDepositNidProof = null;
        failedAttempts = 0;
        currentMode = AtmMode.WELCOME;
        
        // Clear session data
        SessionManager.getInstance().clearSession();
        
        updateOptions();
    }

    private void ejectCard() {
        screenMessage.setText("Card Ejected. Thank you.");
        resetSession();
    }

    public void restoreSession(String cardNumber) {
        this.currentCardNumber = cardNumber;
        this.currentMode = AtmMode.LOGGED_IN;
        this.failedAttempts = 0;

        // Fetch account to greet properly (optional, or just generic welcome back)
        Account acc = apiService.findByCardNumber(cardNumber).orElse(null);
        if (acc != null) {
            screenMessage.setText("Welcome Back, " + acc.getName());
        } else {
            screenMessage.setText("Welcome Back.");
        }
        updateOptions();
    }

    private void enterDisableCardMode() {
        currentMode = AtmMode.DISABLE_ENTER_CARD;
        screenMessage.setText("Enter Card Number to Block/Disable:");
        updateOptions();
    }

    private void enterCardlessDepositMode() {
        currentMode = AtmMode.DEPOSIT_NO_CARD_ACCOUNT;
        screenMessage.setText("Enter Account Number for Deposit:");
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
            optionLeft4.setText("Deposit (Account Number)");
            optionRight1.setText("Forgot PIN");
            optionRight2.setText("Exit");
        } else if (currentMode == AtmMode.LOGGED_IN) {
            optionLeft1.setText("Withdraw");
            optionRight1.setText("Deposit");
            optionLeft2.setText("Check Balance");
            optionRight2.setText("Eject Card");
            optionLeft3.setText("Change PIN");
            optionRight3.setText("Mini Statement");
        } else if (currentMode == AtmMode.CARD_INPUT || currentMode == AtmMode.PIN_INPUT ||
                currentMode == AtmMode.DEPOSIT_INPUT || currentMode == AtmMode.WITHDRAW_INPUT ||
                currentMode == AtmMode.FP_ENTER_CARD || currentMode == AtmMode.FP_ENTER_NID ||
                currentMode == AtmMode.FP_NEW_PIN || currentMode == AtmMode.FP_CONFIRM_PIN ||
                currentMode == AtmMode.CHANGE_PIN_INPUT || currentMode == AtmMode.CHANGE_PIN_CONFIRM ||
                currentMode == AtmMode.DISABLE_ENTER_CARD || currentMode == AtmMode.DISABLE_ENTER_NID ||
                currentMode == AtmMode.DEPOSIT_NO_CARD_ACCOUNT || currentMode == AtmMode.DEPOSIT_NO_CARD_NID
                || currentMode == AtmMode.DEPOSIT_NO_CARD_AMOUNT) {
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
