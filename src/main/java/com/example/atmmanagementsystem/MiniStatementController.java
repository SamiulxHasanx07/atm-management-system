package com.example.atmmanagementsystem;

import com.example.atmmanagementsystem.model.Transaction;
import com.example.atmmanagementsystem.service.AccountService;
import com.example.atmmanagementsystem.service.ApiAccountService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MiniStatementController {

    @FXML
    private ComboBox<String> typeFilter;

    @FXML
    private ComboBox<String> dateFilter;

    @FXML
    private Label transactionCountLabel;

    @FXML
    private TableView<Transaction> trxTable;

    @FXML
    private TableColumn<Transaction, Integer> colId;
    @FXML
    private TableColumn<Transaction, String> colType;
    @FXML
    private TableColumn<Transaction, Double> colAmount;
    @FXML
    private TableColumn<Transaction, String> colDate;

    private String currentCardNumber;
    private final ApiAccountService apiService = (ApiAccountService) AccountService.getInstance();
    private ObservableList<Transaction> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup Columns
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        colAmount.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getAmount()).asObject());

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        colDate.setCellValueFactory(data -> {
            Timestamp ts = data.getValue().getTimestamp();
            return new SimpleStringProperty(ts != null ? ts.toLocalDateTime().format(dtf) : "");
        });

        // Setup Filters
        typeFilter.getItems().addAll("All", "DEPOSIT", "WITHDRAW", "SEND MONEY");
        typeFilter.setValue("All");

        dateFilter.getItems().addAll("All Time", "Last Day", "Last Month");
        dateFilter.setValue("All Time");
    }

    public void setCardNumber(String cardNumber) {
        this.currentCardNumber = cardNumber;
        loadData();
    }

    private void loadData() {
        loadData(null, null, null, null);
    }

    /**
     * Load transactions with optional filters
     */
    private void loadData(String type, String dateFrom, String dateTo, Integer limit) {
        if (currentCardNumber == null) {
            System.err.println("No card number set, cannot load transactions");
            if (transactionCountLabel != null) {
                transactionCountLabel.setText("Error: No card number");
            }
            return;
        }

        System.out.println("=== Loading transactions for card: " + currentCardNumber + " ===");
        System.out.println("Filters - Type: " + type + ", DateFrom: " + dateFrom + 
                          ", DateTo: " + dateTo + ", Limit: " + limit);
        
        try {
            System.out.println("Calling API service...");
            List<Transaction> list = apiService.getTransactions(currentCardNumber, type, dateFrom, dateTo, limit);
            System.out.println("API returned " + list.size() + " transactions");
            
            if (list.isEmpty()) {
                System.out.println("WARNING: No transactions found for this card");
                if (transactionCountLabel != null) {
                    transactionCountLabel.setText("No transactions found");
                }
            } else {
                System.out.println("First transaction: " + list.get(0));
                if (transactionCountLabel != null) {
                    transactionCountLabel.setText("Transactions: " + list.size());
                }
            }
            
            masterData.clear();
            masterData.setAll(list);
            System.out.println("MasterData size: " + masterData.size());
            trxTable.setItems(masterData);
            System.out.println("Table items count: " + trxTable.getItems().size());
            System.out.println("Table visible: " + trxTable.isVisible());
            System.out.println("Table columns: " + trxTable.getColumns().size());
        } catch (Exception e) {
            System.err.println("Failed to load transactions: " + e.getMessage());
            if (transactionCountLabel != null) {
                transactionCountLabel.setText("Error: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    @FXML
    private void onFilterApply() {
        String typeParams = typeFilter.getValue();
        String dateParams = dateFilter.getValue();

        // Convert UI filter values to API parameters
        String type = null;
        boolean transferFilterSelected = "SEND MONEY".equals(typeParams);
        if (!"All".equals(typeParams) && !transferFilterSelected) {
            type = typeParams; // "DEPOSIT" or "WITHDRAW"
        }

        String dateFrom = null;
        String dateTo = null;
        Integer limit = null;

        LocalDate today = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

        if ("Last Day".equals(dateParams)) {
            // Last 24 hours: from yesterday at 00:00:00 to today at 23:59:59
            LocalDate yesterday = today.minusDays(1);
            dateFrom = yesterday.format(dateFormatter);
            dateTo = today.format(dateFormatter);
            System.out.println("Date filter: Last Day");
            System.out.println("  dateFrom: " + dateFrom + " (yesterday 00:00:00)");
            System.out.println("  dateTo: " + dateTo + " (today 23:59:59)");
        } else if ("Last Month".equals(dateParams)) {
            // Last 30 days: from 30 days ago to today
            LocalDate thirtyDaysAgo = today.minusDays(30);
            dateFrom = thirtyDaysAgo.format(dateFormatter);
            dateTo = today.format(dateFormatter);
            System.out.println("Date filter: Last Month (30 days)");
            System.out.println("  dateFrom: " + dateFrom + " (30 days ago 00:00:00)");
            System.out.println("  dateTo: " + dateTo + " (today 23:59:59)");
        } else {
            System.out.println("Date filter: All Time (no date restriction)");
        }
        
        System.out.println("Type filter: " + (type != null ? type : "All"));

        // Reload data from API with filters.
        // For send-money filter, fetch by date and then match transfer aliases locally
        // because backends may use different names (e.g., TRANSFER, SEND_MONEY).
        if (transferFilterSelected) {
            loadData(null, dateFrom, dateTo, limit);
            applyLocalTransferFilter();
        } else {
            loadData(type, dateFrom, dateTo, limit);
        }
    }

    private void applyLocalTransferFilter() {
        List<Transaction> transferOnly = new ArrayList<>();
        for (Transaction transaction : masterData) {
            if (isSendMoneyType(transaction.getType())) {
                transferOnly.add(transaction);
            }
        }
        trxTable.setItems(FXCollections.observableArrayList(transferOnly));
        if (transactionCountLabel != null) {
            transactionCountLabel.setText("Transactions: " + transferOnly.size());
        }
    }

    private boolean isSendMoneyType(String rawType) {
        if (rawType == null) {
            return false;
        }
        String normalized = rawType.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        return normalized.contains("TRANSFER") || "SEND_MONEY".equals(normalized) || "SENDMONEY".equals(normalized);
    }

    @FXML
    private void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("app.fxml"));
            Parent root = loader.load();

            AtmController controller = loader.getController();

            // Return to Logged In state
            controller.restoreSession(currentCardNumber);

            trxTable.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
