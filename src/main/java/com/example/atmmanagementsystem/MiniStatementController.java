package com.example.atmmanagementsystem;

import com.example.atmmanagementsystem.model.Transaction;
import com.example.atmmanagementsystem.service.AccountService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MiniStatementController {

    @FXML
    private ComboBox<String> typeFilter;

    @FXML
    private ComboBox<String> dateFilter;

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
    private final AccountService service = AccountService.getInstance();
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
        typeFilter.getItems().addAll("All", "DEPOSIT", "WITHDRAW");
        typeFilter.setValue("All");

        dateFilter.getItems().addAll("All Time", "Last Day", "Last Month");
        dateFilter.setValue("All Time");
    }

    public void setCardNumber(String cardNumber) {
        this.currentCardNumber = cardNumber;
        loadData();
    }

    private void loadData() {
        if (currentCardNumber == null)
            return;
        List<Transaction> list = service.getTransactions(currentCardNumber);
        masterData.setAll(list);
        applyFilters();
    }

    @FXML
    private void onFilterApply() {
        applyFilters();
    }

    private void applyFilters() {
        String typeParams = typeFilter.getValue();
        String dateParams = dateFilter.getValue();

        FilteredList<Transaction> filtered = new FilteredList<>(masterData, t -> {
            // Type Filter
            if (!"All".equals(typeParams)) {
                if (!t.getType().equalsIgnoreCase(typeParams))
                    return false;
            }

            // Date Filter
            if (!"All Time".equals(dateParams) && t.getTimestamp() != null) {
                LocalDateTime txTime = t.getTimestamp().toLocalDateTime();
                LocalDateTime now = LocalDateTime.now();

                if ("Last Day".equals(dateParams)) {
                    if (txTime.isBefore(now.minusDays(1)))
                        return false;
                } else if ("Last Month".equals(dateParams)) {
                    if (txTime.isBefore(now.minusMonths(1)))
                        return false;
                }
            }

            return true;
        });

        trxTable.setItems(filtered);
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
