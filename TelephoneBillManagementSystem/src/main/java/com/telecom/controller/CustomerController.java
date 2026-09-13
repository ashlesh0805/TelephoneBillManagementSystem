package com.telecom.controller;

import com.telecom.dao.CustomerDAO;
import com.telecom.dao.TariffPlanDAO;
import com.telecom.model.Customer;
import com.telecom.model.TariffPlan;
import com.telecom.service.AuthService;
import com.telecom.util.AlertUtil;
import com.telecom.util.ValidationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Customer Subscriber Management: CRUD operations, plan assignment, and search filters.
 */
public class CustomerController {
    private static final Logger LOGGER = Logger.getLogger(CustomerController.class.getName());

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, Integer> colId;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colAddress;
    @FXML private TableColumn<Customer, String> colPlan;
    @FXML private TableColumn<Customer, String> colStatus;
    @FXML private TableColumn<Customer, Void> colActions;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final TariffPlanDAO planDAO = new TariffPlanDAO();
    private final AuthService authService = AuthService.getInstance();
    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadCustomers();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colPlan.setCellValueFactory(new PropertyValueFactory<>("planName"));

        // Custom Cell for Status Badge
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(status);
                    badge.getStyleClass().add("badge");
                    switch (status.toUpperCase()) {
                        case "ACTIVE" -> badge.getStyleClass().add("badge-active");
                        case "SUSPENDED" -> badge.getStyleClass().add("badge-suspended");
                        default -> badge.getStyleClass().add("badge-inactive");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        // Action Buttons Cell (Edit, Delete)
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(6, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().addAll("btn-secondary", "btn-sm");
                deleteBtn.getStyleClass().addAll("btn-danger", "btn-sm");

                editBtn.setOnAction(event -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    showCustomerDialog(customer);
                });

                deleteBtn.setOnAction(event -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    handleDeleteCustomer(customer);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    deleteBtn.setDisable(!authService.isAdmin());
                    setGraphic(pane);
                }
            }
        });

        customerTable.setItems(customerList);
    }

    private void setupFilters() {
        statusFilterCombo.setItems(FXCollections.observableArrayList("ALL", "ACTIVE", "SUSPENDED", "INACTIVE"));
        statusFilterCombo.setValue("ALL");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> handleSearch());
        statusFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> handleSearch());
    }

    public void loadCustomers() {
        try {
            List<Customer> list = customerDAO.getAllCustomers();
            customerList.setAll(list);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load customers", e);
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        String status = statusFilterCombo.getValue();
        try {
            List<Customer> filtered = customerDAO.searchCustomers(keyword, status);
            customerList.setAll(filtered);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching customers", e);
        }
    }

    @FXML
    private void handleAddCustomer() {
        showCustomerDialog(null);
    }

    private void showCustomerDialog(Customer customerToEdit) {
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle(customerToEdit == null ? "Register New Subscriber" : "Modify Subscriber Details");
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType(customerToEdit == null ? "Add Customer" : "Save Changes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);
        grid.setPadding(new Insets(20, 20, 10, 20));

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. John Doe");
        TextField phoneField = new TextField();
        phoneField.setPromptText("e.g. +919820012345");
        TextField emailField = new TextField();
        emailField.setPromptText("e.g. john@example.com");
        TextField addressField = new TextField();
        addressField.setPromptText("Physical billing address");

        ComboBox<TariffPlan> planCombo = new ComboBox<>();
        try {
            List<TariffPlan> plans = planDAO.getAllPlans();
            planCombo.setItems(FXCollections.observableArrayList(plans));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load plans for dialog", e);
        }

        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("ACTIVE", "SUSPENDED", "INACTIVE"));
        statusCombo.setValue("ACTIVE");

        if (customerToEdit != null) {
            nameField.setText(customerToEdit.getName());
            phoneField.setText(customerToEdit.getPhoneNumber());
            emailField.setText(customerToEdit.getEmail());
            addressField.setText(customerToEdit.getAddress());
            statusCombo.setValue(customerToEdit.getStatus());

            // Select active plan
            for (TariffPlan p : planCombo.getItems()) {
                if (p.getId() == customerToEdit.getPlanId()) {
                    planCombo.setValue(p);
                    break;
                }
            }
        } else if (!planCombo.getItems().isEmpty()) {
            planCombo.setValue(planCombo.getItems().get(0));
        }

        grid.add(new Label("Full Name: *"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Phone Number: *"), 0, 1);
        grid.add(phoneField, 1, 1);
        grid.add(new Label("Email Address:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Billing Address:"), 0, 3);
        grid.add(addressField, 1, 3);
        grid.add(new Label("Tariff Plan: *"), 0, 4);
        grid.add(planCombo, 1, 4);
        grid.add(new Label("Account Status: *"), 0, 5);
        grid.add(statusCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Validation
                String name = nameField.getText();
                String phone = phoneField.getText();
                String email = emailField.getText();
                String address = addressField.getText();
                TariffPlan selectedPlan = planCombo.getValue();
                String status = statusCombo.getValue();

                if (name == null || name.isBlank()) {
                    AlertUtil.showError(dialog.getDialogPane().getScene().getWindow(), "Validation Error", "Customer name cannot be empty.");
                    return null;
                }

                if (!ValidationUtil.isValidPhone(phone)) {
                    AlertUtil.showError(dialog.getDialogPane().getScene().getWindow(), "Validation Error", "Invalid telephone number format.");
                    return null;
                }

                if (email != null && !email.isBlank() && !ValidationUtil.isValidEmail(email)) {
                    AlertUtil.showError(dialog.getDialogPane().getScene().getWindow(), "Validation Error", "Invalid email address format.");
                    return null;
                }

                if (selectedPlan == null) {
                    AlertUtil.showError(dialog.getDialogPane().getScene().getWindow(), "Validation Error", "Please assign a tariff plan.");
                    return null;
                }

                if (customerToEdit == null) {
                    return new Customer(0, name.trim(), phone.trim(), email.trim(), address.trim(),
                            LocalDate.now().toString(), selectedPlan.getId(), selectedPlan.getName(), status);
                } else {
                    customerToEdit.setName(name.trim());
                    customerToEdit.setPhoneNumber(phone.trim());
                    customerToEdit.setEmail(email.trim());
                    customerToEdit.setAddress(address.trim());
                    customerToEdit.setPlanId(selectedPlan.getId());
                    customerToEdit.setPlanName(selectedPlan.getName());
                    customerToEdit.setStatus(status);
                    return customerToEdit;
                }
            }
            return null;
        });

        Optional<Customer> result = dialog.showAndWait();
        result.ifPresent(customer -> {
            try {
                if (customerToEdit == null) {
                    customerDAO.createCustomer(customer);
                    AlertUtil.showInfo(customerTable.getScene().getWindow(), "Customer Added", "Subscriber successfully registered.");
                } else {
                    customerDAO.updateCustomer(customer);
                    AlertUtil.showInfo(customerTable.getScene().getWindow(), "Customer Updated", "Subscriber details updated successfully.");
                }
                loadCustomers();
            } catch (SQLException e) {
                AlertUtil.showError(customerTable.getScene().getWindow(), "Database Error", "Failed to save customer. " + e.getMessage());
            }
        });
    }

    private void handleDeleteCustomer(Customer customer) {
        boolean confirm = AlertUtil.showConfirmation(customerTable.getScene().getWindow(),
                "Confirm Subscriber Deletion",
                "Are you sure you want to delete customer '" + customer.getName() + "'?\nThis will remove all associated call logs and invoices.");
        if (confirm) {
            try {
                customerDAO.deleteCustomer(customer.getId());
                loadCustomers();
                AlertUtil.showInfo(customerTable.getScene().getWindow(), "Deleted", "Customer deleted successfully.");
            } catch (SQLException e) {
                AlertUtil.showError(customerTable.getScene().getWindow(), "Deletion Error", "Could not delete customer. " + e.getMessage());
            }
        }
    }
}
