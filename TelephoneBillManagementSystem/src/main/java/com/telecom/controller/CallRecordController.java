package com.telecom.controller;

import com.telecom.dao.CallRecordDAO;
import com.telecom.dao.CustomerDAO;
import com.telecom.dao.TariffPlanDAO;
import com.telecom.model.CallRecord;
import com.telecom.model.CallType;
import com.telecom.model.Customer;
import com.telecom.model.TariffPlan;
import com.telecom.service.BillingEngine;
import com.telecom.service.CallService;
import com.telecom.service.CsvService;
import com.telecom.util.AlertUtil;
import com.telecom.util.CurrencyUtil;
import com.telecom.util.DateUtil;
import com.telecom.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for managing Call Detail Records (CDR), manual entry with real-time prefix detection,
 * and bulk CSV imports.
 */
public class CallRecordController {
    private static final Logger LOGGER = Logger.getLogger(CallRecordController.class.getName());

    @FXML private ComboBox<Customer> customerFilterCombo;
    @FXML private ComboBox<String> callTypeFilterCombo;
    @FXML private TableView<CallRecord> callTable;
    @FXML private TableColumn<CallRecord, Integer> colId;
    @FXML private TableColumn<CallRecord, String> colTimestamp;
    @FXML private TableColumn<CallRecord, String> colCustomer;
    @FXML private TableColumn<CallRecord, String> colDestination;
    @FXML private TableColumn<CallRecord, String> colType;
    @FXML private TableColumn<CallRecord, String> colDuration;
    @FXML private TableColumn<CallRecord, String> colCost;
    @FXML private TableColumn<CallRecord, Void> colActions;

    private final CallRecordDAO callDAO = new CallRecordDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final TariffPlanDAO planDAO = new TariffPlanDAO();
    private final BillingEngine billingEngine = new BillingEngine();
    private final CsvService csvService = new CsvService();

    private final ObservableList<CallRecord> allCallsList = FXCollections.observableArrayList();
    private final ObservableList<CallRecord> filteredCallsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadCustomersFilter();
        loadCalls();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTimestamp.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(DateUtil.formatDisplayDateTime(cellData.getValue().getCallTimestamp())));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colDestination.setCellValueFactory(new PropertyValueFactory<>("destinationNumber"));

        // Call Type Badge Cell
        colType.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCallType().getDisplayName()));
        colType.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");
                    if (item.contains("Local")) {
                        badge.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1;");
                    } else if (item.contains("STD")) {
                        badge.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #b45309;");
                    } else {
                        badge.setStyle("-fx-background-color: #fce7f3; -fx-text-fill: #be185d;");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        colDuration.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedDuration()));

        colCost.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(CurrencyUtil.format(cellData.getValue().getComputedCost())));

        // Action Cell (Delete call)
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            {
                deleteBtn.getStyleClass().addAll("btn-danger", "btn-sm");
                deleteBtn.setOnAction(event -> {
                    CallRecord call = getTableView().getItems().get(getIndex());
                    handleDeleteCall(call);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        callTable.setItems(filteredCallsList);
    }

    private void setupFilters() {
        callTypeFilterCombo.setItems(FXCollections.observableArrayList("ALL", "LOCAL", "STD", "ISD"));
        callTypeFilterCombo.setValue("ALL");

        customerFilterCombo.valueProperty().addListener((obs, oldV, newV) -> applyFilters());
        callTypeFilterCombo.valueProperty().addListener((obs, oldV, newV) -> applyFilters());
    }

    private void loadCustomersFilter() {
        try {
            List<Customer> customers = customerDAO.getAllCustomers();
            Customer allPlaceholder = new Customer(0, "All Customers", "", "", "", "", 0, "", "");
            customers.add(0, allPlaceholder);
            customerFilterCombo.setItems(FXCollections.observableArrayList(customers));
            customerFilterCombo.setValue(allPlaceholder);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading customers filter", e);
        }
    }

    public void loadCalls() {
        try {
            List<CallRecord> list = callDAO.getAllCalls();
            allCallsList.setAll(list);
            applyFilters();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load call records", e);
        }
    }

    private void applyFilters() {
        Customer selectedCust = customerFilterCombo.getValue();
        String selectedType = callTypeFilterCombo.getValue();

        List<CallRecord> filtered = allCallsList.stream().filter(call -> {
            boolean matchCust = selectedCust == null || selectedCust.getId() == 0 || call.getCustomerId() == selectedCust.getId();
            boolean matchType = selectedType == null || selectedType.equalsIgnoreCase("ALL") ||
                    call.getCallType().name().equalsIgnoreCase(selectedType);
            return matchCust && matchType;
        }).toList();

        filteredCallsList.setAll(filtered);
    }

    @FXML
    private void handleLogCall() {
        Dialog<CallRecord> dialog = new Dialog<>();
        dialog.setTitle("Log Telephone Call");
        dialog.setHeaderText("Record an individual call in the Call Detail Register (CDR)");

        ButtonType logBtnType = new ButtonType("Save Call", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(logBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);
        grid.setPadding(new Insets(20, 20, 10, 20));

        ComboBox<Customer> customerBox = new ComboBox<>();
        try {
            customerBox.setItems(FXCollections.observableArrayList(customerDAO.getAllCustomers()));
            if (!customerBox.getItems().isEmpty()) {
                customerBox.setValue(customerBox.getItems().get(0));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load customers for modal", e);
        }

        TextField destNumberField = new TextField();
        destNumberField.setPromptText("e.g. +14155552671, 02224567890, +919820011223");

        Label detectedTypeBadge = new Label("Detected: LOCAL");
        detectedTypeBadge.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1; -fx-padding: 3px 8px; -fx-background-radius: 6px; -fx-font-weight: bold;");

        TextField durationField = new TextField("180");
        durationField.setPromptText("Duration in seconds");

        Label costPreviewLabel = new Label("Estimated Cost: ₹0.00");
        costPreviewLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #16a34a;");

        // Real-time prefix detection listener
        destNumberField.textProperty().addListener((obs, oldV, newV) -> {
            CallType detected = CallService.detectCallType(newV);
            detectedTypeBadge.setText("Detected: " + detected.getDisplayName());
            if (detected == CallType.LOCAL) {
                detectedTypeBadge.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1; -fx-padding: 3px 8px; -fx-background-radius: 6px; -fx-font-weight: bold;");
            } else if (detected == CallType.STD) {
                detectedTypeBadge.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #b45309; -fx-padding: 3px 8px; -fx-background-radius: 6px; -fx-font-weight: bold;");
            } else {
                detectedTypeBadge.setStyle("-fx-background-color: #fce7f3; -fx-text-fill: #be185d; -fx-padding: 3px 8px; -fx-background-radius: 6px; -fx-font-weight: bold;");
            }
            updateCostPreview(customerBox.getValue(), destNumberField.getText(), durationField.getText(), costPreviewLabel);
        });

        durationField.textProperty().addListener((obs, oldV, newV) ->
                updateCostPreview(customerBox.getValue(), destNumberField.getText(), durationField.getText(), costPreviewLabel));
        customerBox.valueProperty().addListener((obs, oldV, newV) ->
                updateCostPreview(customerBox.getValue(), destNumberField.getText(), durationField.getText(), costPreviewLabel));

        grid.add(new Label("Subscriber: *"), 0, 0);
        grid.add(customerBox, 1, 0);
        grid.add(new Label("Destination Number: *"), 0, 1);
        grid.add(destNumberField, 1, 1);
        grid.add(new Label("Call Classification:"), 0, 2);
        grid.add(detectedTypeBadge, 1, 2);
        grid.add(new Label("Duration (seconds): *"), 0, 3);
        grid.add(durationField, 1, 3);
        grid.add(new Label("Tariff Estimation:"), 0, 4);
        grid.add(costPreviewLabel, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == logBtnType) {
                Customer cust = customerBox.getValue();
                String dest = destNumberField.getText();
                String durStr = durationField.getText();

                if (cust == null) {
                    AlertUtil.showError(dialog.getDialogPane().getScene().getWindow(), "Validation Error", "Please select a subscriber.");
                    return null;
                }
                if (!CallService.isValidPhoneNumber(dest)) {
                    AlertUtil.showError(dialog.getDialogPane().getScene().getWindow(), "Validation Error", "Please enter a valid destination phone number.");
                    return null;
                }
                if (!ValidationUtil.isPositiveInteger(durStr)) {
                    AlertUtil.showError(dialog.getDialogPane().getScene().getWindow(), "Validation Error", "Duration must be a positive number of seconds.");
                    return null;
                }

                int duration = Integer.parseInt(durStr.trim());
                CallType type = CallService.detectCallType(dest);

                try {
                    TariffPlan plan = planDAO.getPlanById(cust.getPlanId());
                    double cost = billingEngine.computeCallCost(type, duration, plan);

                    CallRecord call = new CallRecord();
                    call.setCustomerId(cust.getId());
                    call.setCallTimestamp(DateUtil.nowDateTimeString());
                    call.setDestinationNumber(dest.trim());
                    call.setCallType(type);
                    call.setDurationSeconds(duration);
                    call.setComputedCost(cost);
                    return call;
                } catch (SQLException e) {
                    AlertUtil.showError(dialog.getDialogPane().getScene().getWindow(), "Error", "Could not fetch subscriber tariff plan.");
                    return null;
                }
            }
            return null;
        });

        Optional<CallRecord> result = dialog.showAndWait();
        result.ifPresent(call -> {
            try {
                callDAO.createCall(call);
                loadCalls();
                AlertUtil.showInfo(callTable.getScene().getWindow(), "Call Logged", "Call record successfully registered in CDR.");
            } catch (SQLException e) {
                AlertUtil.showError(callTable.getScene().getWindow(), "Database Error", "Failed to save call record. " + e.getMessage());
            }
        });
    }

    private void updateCostPreview(Customer cust, String dest, String durationStr, Label label) {
        if (cust == null || dest == null || durationStr == null || !ValidationUtil.isPositiveInteger(durationStr)) {
            label.setText("Estimated Cost: ₹0.00");
            return;
        }
        try {
            TariffPlan plan = planDAO.getPlanById(cust.getPlanId());
            if (plan != null) {
                CallType type = CallService.detectCallType(dest);
                int duration = Integer.parseInt(durationStr.trim());
                double cost = billingEngine.computeCallCost(type, duration, plan);
                label.setText("Estimated Cost: " + CurrencyUtil.format(cost) + " (" + type.getDisplayName() + ")");
            }
        } catch (SQLException ignored) {}
    }

    @FXML
    private void handleBulkImportCsv() {
        // Step 1: Pick Customer
        ChoiceDialog<Customer> custDialog;
        try {
            List<Customer> list = customerDAO.getAllCustomers();
            if (list.isEmpty()) {
                AlertUtil.showWarning(callTable.getScene().getWindow(), "No Subscribers", "Please register a customer before importing call logs.");
                return;
            }
            custDialog = new ChoiceDialog<>(list.get(0), list);
            custDialog.setTitle("Select Subscriber for Import");
            custDialog.setHeaderText("Choose the customer account to assign these imported call records to:");
            custDialog.setContentText("Subscriber:");
        } catch (SQLException e) {
            AlertUtil.showError(callTable.getScene().getWindow(), "Database Error", "Could not load subscribers.");
            return;
        }

        Optional<Customer> custOpt = custDialog.showAndWait();
        if (custOpt.isEmpty()) return;
        Customer selectedCust = custOpt.get();

        // Step 2: Pick CSV File
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Call Records CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        
        // Suggest the bundled sample CSV file if it exists
        File sampleDir = new File("src/main/resources/sample_data");
        if (sampleDir.exists()) {
            fileChooser.setInitialDirectory(sampleDir);
        }

        File selectedFile = fileChooser.showOpenDialog(callTable.getScene().getWindow());
        if (selectedFile != null) {
            try {
                int count = csvService.importCallRecordsCsv(selectedCust.getId(), selectedFile);
                loadCalls();
                AlertUtil.showInfo(callTable.getScene().getWindow(), "Import Successful",
                        "Successfully imported " + count + " call records for subscriber " + selectedCust.getName() + "!");
            } catch (Exception e) {
                AlertUtil.showError(callTable.getScene().getWindow(), "Import Failed", "Error parsing CSV file: " + e.getMessage());
            }
        }
    }

    private void handleDeleteCall(CallRecord call) {
        boolean confirm = AlertUtil.showConfirmation(callTable.getScene().getWindow(), "Delete CDR Entry",
                "Delete call log to " + call.getDestinationNumber() + "?");
        if (confirm) {
            try {
                callDAO.deleteCall(call.getId());
                loadCalls();
            } catch (SQLException e) {
                AlertUtil.showError(callTable.getScene().getWindow(), "Error", "Failed to delete call record.");
            }
        }
    }
}
