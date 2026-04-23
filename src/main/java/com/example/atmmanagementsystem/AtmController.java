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
        DEPOSIT_NO_CARD_ACCOUNT, DEPOSIT_NO_CARD_NID, DEPOSIT_NO_CARD_AMOUNT,
        TRANSFER_TYPE_SELECT, TRANSFER_ACCOUNT_INPUT, TRANSFER_CARD_INPUT, TRANSFER_AMOUNT_INPUT
    }

    private enum TransferType {
        ACCOUNT,
        CARD
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
    private TransferType transferType;
    private String transferRecipientValue;
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
        } else if (currentMode == AtmMode.TRANSFER_TYPE_SELECT) {
            System.out.println("Routing to TRANSFER_TYPE_SELECT prompt");
            screenMessage.setText("Choose transfer type: L1(Account) or R1(Card).");
        } else if (currentMode == AtmMode.TRANSFER_ACCOUNT_INPUT) {
            System.out.println("Routing to handleTransferAccountInput");
            handleTransferAccountInput(input);
        } else if (currentMode == AtmMode.TRANSFER_CARD_INPUT) {
            System.out.println("Routing to handleTransferCardInput");
            handleTransferCardInput(input);
        } else if (currentMode == AtmMode.TRANSFER_AMOUNT_INPUT) {
            System.out.println("Routing to handleTransferAmountInput");
            handleTransferAmountInput(input);
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

        runApiTask(() -> apiService.login(currentCardNumber, inputPin),
                loginResponse -> {
                    if (loginResponse.isSuccess() && loginResponse.getToken() != null) {
                        // Success - token and user info are already stored in SessionManager by
                        // ApiService
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
                },
                e -> {
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
                });
    }

    private void handleDepositInput(String input) {
        try {
            System.out.println("=== DEPOSIT START ===");
            System.out.println("Current Card Number: " + currentCardNumber);
            System.out.println("Session: " + SessionManager.getInstance());
            System.out.println("Amount: " + input);

            double amount = Double.parseDouble(input);

            runApiTaskVoid(() -> {
                apiService.deposit(currentCardNumber, amount);
                ((ApiAccountService) apiService).refreshSessionBalance(currentCardNumber);
            }, () -> {
                String balanceStr = String.format("%.2f", SessionManager.getInstance().getBalance());
                System.out.println("Deposit successful. New balance from API: " + balanceStr);
                screenMessage
                        .setText("Deposited " + String.format("%.0f", amount) + " TK. Balance: " + balanceStr + " TK");
                currentMode = AtmMode.LOGGED_IN;
                updateOptions();
            }, e -> {
                if (e instanceof IllegalArgumentException) {
                    screenMessage.setText(e.getMessage());
                    System.err.println("Deposit error: " + e.getMessage());
                } else {
                    screenMessage.setText("Deposit Error: " + e.getMessage());
                    System.err.println("Deposit exception:");
                    e.printStackTrace();
                }
            });
        } catch (NumberFormatException e) {
            screenMessage.setText("Invalid amount. Enter numbers only.");
        }
    }

    private void handleWithdrawInput(String input) {
        try {
            System.out.println("=== WITHDRAW START ===");
            System.out.println("Current Card Number: " + currentCardNumber);
            System.out.println("Session: " + SessionManager.getInstance());
            System.out.println("Amount: " + input);

            double amount = Double.parseDouble(input);

            runApiTaskVoid(() -> {
                apiService.withdraw(currentCardNumber, amount);
                ((ApiAccountService) apiService).refreshSessionBalance(currentCardNumber);
            }, () -> {
                String balanceStr = String.format("%.2f", SessionManager.getInstance().getBalance());
                System.out.println("Withdraw successful. New balance from API: " + balanceStr);
                screenMessage
                        .setText("Withdrawn " + String.format("%.0f", amount) + " TK. Balance: " + balanceStr + " TK");
                currentMode = AtmMode.LOGGED_IN;
                updateOptions();
            }, e -> {
                if (e instanceof IllegalArgumentException) {
                    screenMessage.setText(e.getMessage());
                    System.err.println("Withdraw error: " + e.getMessage());
                } else {
                    screenMessage.setText("Withdraw Error: " + e.getMessage());
                    System.err.println("Withdraw exception:");
                    e.printStackTrace();
                }
            });
        } catch (NumberFormatException e) {
            screenMessage.setText("Invalid amount. Enter numbers only.");
        }
    }

    private void handleForgotPinCardInput(String inputCard) {
        if (inputCard == null || inputCard.length() < 10) {
            screenMessage.setText("Invalid Card Number. Try again.");
            return;
        }

        currentCardNumber = inputCard;
        currentMode = AtmMode.FP_ENTER_NID;
        screenMessage.setText("Enter NID Proof:");
        updateOptions();
    }

    private void handleForgotPinNidInput(String input) {
        if (input == null || input.isEmpty()) {
            screenMessage.setText("Enter valid NID proof.");
            return;
        }

        runApiTask(() -> apiService.resetPin(currentCardNumber, input),
                newPin -> {
                    resetSession();
                    screenMessage.setText("Success! New PIN: " + newPin + "\nPlease save it and login.");
                },
                e -> {
                    screenMessage.setText("Error: " + e.getMessage());
                    currentMode = AtmMode.FP_ENTER_NID;
                    updateOptions();
                });
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

        runApiTaskVoid(() -> apiService.updatePin(currentCardNumber, confirmPin),
                () -> {
                    screenMessage.setText("PIN Changed Successfully.");
                    currentMode = AtmMode.LOGGED_IN;
                    tempNewPin = null;
                    updateOptions();
                },
                e -> {
                    if (e instanceof IllegalArgumentException) {
                        screenMessage.setText(e.getMessage());
                        currentMode = AtmMode.CHANGE_PIN_INPUT; // Let them try again
                        updateOptions();
                    } else {
                        screenMessage.setText("Error: " + e.getMessage());
                    }
                });
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

        currentCardNumber = inputCard;
        currentMode = AtmMode.DISABLE_ENTER_NID;
        screenMessage.setText("Enter NID Proof to BLOCK (Last 4 Digits):");
        updateOptions();
    }

    private void handleDisableEnterNid(String input) {
        if (input == null || input.isEmpty()) {
            screenMessage.setText("Enter valid NID proof.");
            return;
        }

        runApiTaskVoid(() -> apiService.blockAccount(currentCardNumber, input),
                () -> {
                    screenMessage.setText("Card Successfully BLOCKED. Please contact bank.");
                    resetSession();
                },
                e -> screenMessage.setText("Error blocking card: " + e.getMessage()));
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

        // Store NID proof and proceed to amount entry
        cardlessDepositNidProof = input;
        currentMode = AtmMode.DEPOSIT_NO_CARD_AMOUNT;
        screenMessage.setText("Enter Amount to Deposit (Multiple of 500):");
        updateOptions();
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

            runApiTaskVoid(
                    () -> apiService.cardlessDeposit(cardlessDepositAccountNumber, cardlessDepositNidProof, amount),
                    () -> {
                        System.out.println("Cardless deposit successful");

                        // Clear cardless deposit data
                        cardlessDepositAccountNumber = null;
                        cardlessDepositNidProof = null;

                        // Store message before reset
                        String successMsg = "Success! Deposited " + String.format("%.0f", amount) + " TK.";

                        resetSession();

                        // Set message after reset so it's visible
                        screenMessage.setText(successMsg);
                    },
                    e -> {
                        if (e instanceof IllegalArgumentException) {
                            screenMessage.setText(e.getMessage());
                            System.err.println("Cardless deposit validation error: " + e.getMessage());
                        } else {
                            screenMessage.setText("Cardless Deposit Error: " + e.getMessage());
                            System.err.println("Cardless deposit exception:");
                            e.printStackTrace();
                        }
                    });
        } catch (NumberFormatException e) {
            screenMessage.setText("Invalid amount. Enter numbers only.");
            System.err.println("Cardless deposit amount parse error: " + e.getMessage());
        }
    }

    private void handleTransferAccountInput(String input) {
        if (input == null || !input.matches("\\d{12}")) {
            screenMessage.setText("Invalid account number. Enter exactly 12 digits.");
            return;
        }

        transferType = TransferType.ACCOUNT;
        transferRecipientValue = input;
        currentMode = AtmMode.TRANSFER_AMOUNT_INPUT;
        screenMessage.setText("Enter Amount (Min 500, Multiple of 500, Max 500000):");
        updateOptions();
    }

    private void handleTransferCardInput(String input) {
        if (input == null || !input.matches("\\d{16}")) {
            screenMessage.setText("Invalid card number. Enter exactly 16 digits.");
            return;
        }

        if (currentCardNumber != null && currentCardNumber.equals(input)) {
            screenMessage.setText("You cannot transfer to your own card.");
            return;
        }

        transferType = TransferType.CARD;
        transferRecipientValue = input;
        currentMode = AtmMode.TRANSFER_AMOUNT_INPUT;
        screenMessage.setText("Enter Amount (Min 500, Multiple of 500, Max 500000):");
        updateOptions();
    }

    private void handleTransferAmountInput(String input) {
        if (transferType == null || transferRecipientValue == null || transferRecipientValue.isEmpty()) {
            currentMode = AtmMode.LOGGED_IN;
            screenMessage.setText("Transfer session expired. Please start again.");
            updateOptions();
            return;
        }

        if (input == null || !input.matches("\\d+")) {
            screenMessage.setText("Invalid amount. Enter numbers only.");
            return;
        }

        try {
            long amount = Long.parseLong(input);

            if (amount < 500) {
                screenMessage.setText("Minimum transfer amount is 500 TK.");
                return;
            }
            if (amount % 500 != 0) {
                screenMessage.setText("Amount must be a multiple of 500.");
                return;
            }
            if (amount > 500000) {
                screenMessage.setText("Maximum amount per transfer is 500000 TK.");
                return;
            }

            runApiTaskVoid(() -> {
                if (transferType == TransferType.ACCOUNT) {
                    apiService.transferToAccount(currentCardNumber, transferRecipientValue, amount);
                } else {
                    apiService.transferToCard(currentCardNumber, transferRecipientValue, amount);
                }
                ((ApiAccountService) apiService).refreshSessionBalance(currentCardNumber);
            }, () -> {
                String targetLabel = transferType == TransferType.ACCOUNT ? "account" : "card";
                String successMsg = "Transfer successful. Sent " + amount + " TK to " + targetLabel + " "
                        + transferRecipientValue + ". Balance: "
                        + String.format("%.2f", SessionManager.getInstance().getBalance()) + " TK";

                transferType = null;
                transferRecipientValue = null;
                currentMode = AtmMode.LOGGED_IN;
                screenMessage.setText(successMsg);
                updateOptions();
            }, e -> {
                String msg = e.getMessage() == null ? "Transfer failed" : e.getMessage();
                handleTransferError(msg);
            });

        } catch (NumberFormatException e) {
            screenMessage.setText("Invalid amount format.");
        }
    }

    private void handleTransferError(String errorMessage) {
        String message = errorMessage;
        if (message.startsWith("HTTP_")) {
            int separator = message.indexOf(":");
            String statusPart = separator > 0 ? message.substring(5, separator) : "";
            if ("401".equals(statusPart) || "403".equals(statusPart)) {
                resetSession();
                screenMessage.setText("Authorization failed. Please login again.");
                return;
            }
            if (separator > 0 && separator + 1 < message.length()) {
                message = message.substring(separator + 1).trim();
            }
        }
        screenMessage.setText(message);
    }

    private void handleOption(String optionCode) {
        switch (currentMode) {
            case WELCOME:
                handleWelcomeOptions(optionCode);
                break;
            case TRANSFER_TYPE_SELECT:
                if (optionCode.equals("L1")) {
                    transferType = TransferType.ACCOUNT;
                    currentMode = AtmMode.TRANSFER_ACCOUNT_INPUT;
                    screenMessage.setText("Enter Recipient Account Number (12 digits):");
                    updateOptions();
                } else if (optionCode.equals("R1")) {
                    transferType = TransferType.CARD;
                    currentMode = AtmMode.TRANSFER_CARD_INPUT;
                    screenMessage.setText("Enter Recipient Card Number (16 digits):");
                    updateOptions();
                } else if (optionCode.equals("R4")) {
                    transferType = null;
                    transferRecipientValue = null;
                    currentMode = AtmMode.LOGGED_IN;
                    screenMessage.setText("Transfer Cancelled.");
                    updateOptions();
                }
                break;
            case CARD_INPUT:
            case PIN_INPUT:
            case DEPOSIT_INPUT:
            case WITHDRAW_INPUT:
            case FP_ENTER_CARD:
            case FP_ENTER_NID:
            case DISABLE_ENTER_CARD:
            case DISABLE_ENTER_NID:
            case DEPOSIT_NO_CARD_ACCOUNT:
            case DEPOSIT_NO_CARD_NID:
            case DEPOSIT_NO_CARD_AMOUNT:
            case TRANSFER_ACCOUNT_INPUT:
            case TRANSFER_CARD_INPUT:
            case TRANSFER_AMOUNT_INPUT:
                // We'll allow "Exit" or "Cancel" if mapped
                if (optionCode.equals("R4")) { // Assume R4 is Exit/Cancel roughly
                    // If in txn mode, go back to logged in? Or Eject?
                    if (currentMode == AtmMode.DEPOSIT_INPUT || currentMode == AtmMode.WITHDRAW_INPUT
                            || currentMode == AtmMode.TRANSFER_TYPE_SELECT
                            || currentMode == AtmMode.TRANSFER_ACCOUNT_INPUT
                            || currentMode == AtmMode.TRANSFER_CARD_INPUT
                            || currentMode == AtmMode.TRANSFER_AMOUNT_INPUT) {
                        currentMode = AtmMode.LOGGED_IN;
                        transferType = null;
                        transferRecipientValue = null;
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
            case "L3": // Send Money
                enterTransferMode();
                break;
            case "L4": // Change PIN
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

    private void enterTransferMode() {
        transferType = null;
        transferRecipientValue = null;
        currentMode = AtmMode.TRANSFER_TYPE_SELECT;
        screenMessage.setText("Send Money: Choose L1(Account) or R1(Card).");
        updateOptions();
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

    @FXML
    protected void onMoreDetails() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("info.fxml"));
            javafx.scene.Parent root = loader.load();
            screenMessage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            screenMessage.setText("Error loading Info screen.");
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
        transferType = null;
        transferRecipientValue = null;
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

        runApiTask(() -> apiService.findByCardNumber(cardNumber).orElse(null),
                acc -> {
                    if (acc != null) {
                        screenMessage.setText("Welcome Back, " + acc.getName());
                    } else {
                        screenMessage.setText("Welcome Back.");
                    }
                    updateOptions();
                },
                e -> {
                    screenMessage.setText("Welcome Back.");
                    updateOptions();
                });
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
            optionLeft3.setText("Send Money");
            optionRight3.setText("Mini Statement");
            optionLeft4.setText("Change PIN");
        } else if (currentMode == AtmMode.TRANSFER_TYPE_SELECT) {
            optionLeft1.setText("To Account");
            optionRight1.setText("To Card");
            optionRight4.setText("Cancel");
        } else if (currentMode == AtmMode.CARD_INPUT || currentMode == AtmMode.PIN_INPUT ||
                currentMode == AtmMode.DEPOSIT_INPUT || currentMode == AtmMode.WITHDRAW_INPUT ||
                currentMode == AtmMode.FP_ENTER_CARD || currentMode == AtmMode.FP_ENTER_NID ||
                currentMode == AtmMode.CHANGE_PIN_INPUT || currentMode == AtmMode.CHANGE_PIN_CONFIRM ||
                currentMode == AtmMode.DISABLE_ENTER_CARD || currentMode == AtmMode.DISABLE_ENTER_NID ||
                currentMode == AtmMode.DEPOSIT_NO_CARD_ACCOUNT || currentMode == AtmMode.DEPOSIT_NO_CARD_NID
                || currentMode == AtmMode.DEPOSIT_NO_CARD_AMOUNT
                || currentMode == AtmMode.TRANSFER_ACCOUNT_INPUT || currentMode == AtmMode.TRANSFER_CARD_INPUT
                || currentMode == AtmMode.TRANSFER_AMOUNT_INPUT) {
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

    private Timeline loadingTimeline;

    private void startLoading() {
        if (loadingTimeline != null) {
            loadingTimeline.stop();
        }
        loadingTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> screenMessage.setText("Processing.")),
                new KeyFrame(Duration.millis(300), e -> screenMessage.setText("Processing..")),
                new KeyFrame(Duration.millis(600), e -> screenMessage.setText("Processing...")));
        loadingTimeline.setCycleCount(Timeline.INDEFINITE);
        loadingTimeline.play();
    }

    private void stopLoading() {
        if (loadingTimeline != null) {
            loadingTimeline.stop();
        }
    }

    private <T> void runApiTask(java.util.concurrent.Callable<T> apiCall,
            java.util.function.Consumer<T> onSuccess,
            java.util.function.Consumer<Throwable> onError) {
        startLoading();
        javafx.concurrent.Task<T> task = new javafx.concurrent.Task<T>() {
            @Override
            protected T call() throws Exception {
                return apiCall.call();
            }
        };

        task.setOnSucceeded(e -> {
            stopLoading();
            if (onSuccess != null)
                onSuccess.accept(task.getValue());
        });

        task.setOnFailed(e -> {
            stopLoading();
            if (onError != null)
                onError.accept(task.getException());
        });

        new Thread(task).start();
    }

    private void runApiTaskVoid(Runnable apiCall,
            Runnable onSuccess,
            java.util.function.Consumer<Throwable> onError) {
        startLoading();
        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<Void>() {
            @Override
            protected Void call() throws Exception {
                apiCall.run();
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            stopLoading();
            if (onSuccess != null)
                onSuccess.run();
        });

        task.setOnFailed(e -> {
            stopLoading();
            if (onError != null)
                onError.accept(task.getException());
        });

        new Thread(task).start();
    }
}
