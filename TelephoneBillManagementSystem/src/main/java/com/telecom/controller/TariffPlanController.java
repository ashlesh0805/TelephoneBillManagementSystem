package com.telecom.controller;

import com.telecom.dao.TariffPlanDAO;
import com.telecom.model.TariffPlan;
import com.telecom.service.AuthService;
import com.telecom.util.AlertUtil;
import com.telecom.util.CurrencyUtil;
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
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for managing Tariff Plans, calling rates (Local, STD, ISD), and allowances.
 */
public class TariffPlanController {
    private static final Logger LOGGER = Logger.getLogger(TariffPlanController.class.getName());

    @FXML private TableView<TariffPlan> planTable;
    @FXML private TableColumn<TariffPlan, Integer> colId;
    @FXML private TableColumn<TariffPlan, String> colName;
    @FXML private TableColumn<TariffPlan, String> colRental;
    @FXML private TableColumn<TariffPlan, String> colFreeMins;
    @FXML private TableColumn<TariffPlan, String> colLocal;
    @FXML private TableColumn<TariffPlan, String> colStd;
    @FXML private TableColumn<TariffPlan, String> colIsd;
    @FXML private TableColumn<TariffPlan, String> colSubscribers;
    @FXML private TableColumn<TariffPlan, Void> colActions;
    @FXML private Button addPlanButton;

    private final TariffPlanDAO planDAO = new TariffPlanDAO();
    private final AuthService authService = AuthService.getInstance();
    private final ObservableList<TariffPlan> planList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadPlans();

        if (addPlanButton != null) {
            addPlanButton.setDisable(!authService.isAdmin());
        }
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colRental.setCellValueFactory(cellData ->
                new SimpleStringProperty(CurrencyUtil.format(cellData.getValue().getMonthlyRental())));
        colFreeMins.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFreeMinutes() + " mins"));
        colLocal.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("₹%.2f/min", cellData.getValue().getRatePerMinLocal())));
        colStd.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("₹%.2f/min", cellData.getValue().getRatePerMinStd())));
        colIsd.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("₹%.2f/min", cellData.getValue().getRatePerMinIsd())));

        colSubscribers.setCellValueFactory(cellData -> {
            try {
                int count = planDAO.getCustomerCountForPlan(cellData.getValue().getId());
                return new SimpleStringProperty(count + " users");
            } catch (SQLException e) {
                return new SimpleStringProperty("0 users");
            }
        });

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(6, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().addAll("btn-secondary", "btn-sm");
                deleteBtn.getStyleClass().addAll("btn-danger", "btn-sm");

                editBtn.setOnAction(e -> {
                    TariffPlan plan = getTableView().getItems().get(getIndex());
                    showPlanDialog(plan);
                });

                deleteBtn.setOnAction(e -> {
                    TariffPlan plan = getTableView().getItems().get(getIndex());
                    handleDeletePlan(plan);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    boolean isAdmin = authService.isAdmin();
                    editBtn.setDisable(!isAdmin);
                    deleteBtn.setDisable(!isAdmin);
                    setGraphic(pane);
                }
            }
        });

        planTable.setItems(planList);
    }

    public void loadPlans() {
        try {
            List<TariffPlan> list = planDAO.getAllPlans();
            planList.setAll(list);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load tariff plans", e);
        }
    }

    @FXML
    private void handleAddPlan() {
        showPlanDialog(null);
    }

    private void showPlanDialog(TariffPlan planToEdit) {
        Dialog<TariffPlan> dialog = new Dialog<>();
        dialog.setTitle(planToEdit == null ? "Create Tariff Package" : "Modify Tariff Package");
        dialog.setHeaderText("Specify calling rates, monthly rental, and free minutes allowance");

        ButtonType saveBtnType = new ButtonType(planToEdit == null ? "Create Plan" : "Update Plan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 20));

        TextField nameField = new TextField();
        TextField rentalField = new TextField("199.00");
        TextField freeMinsField = new TextField("100");
        TextField localRateField = new TextField("0.50");
        TextField stdRateField = new TextField("1.20");
        TextField isdRateField = new TextField("6.50");
        TextField smsRateField = new TextField("0.25");
        TextField descField = new TextField();

        if (planToEdit != null) {
            nameField.setText(planToEdit.getName());
            rentalField.setText(String.format("%.2f", planToEdit.getMonthlyRental()));
            freeMinsField.setText(String.valueOf(planToEdit.getFreeMinutes()));
            localRateField.setText(String.format("%.2f", planToEdit.getRatePerMinLocal()));
            stdRateField.setText(String.format("%.2f", planToEdit.getRatePerMinStd()));
            isdRateField.setText(String.format("%.2f", planToEdit.getRatePerMinIsd()));
            smsRateField.setText(String.format("%.2f", planToEdit.getSmsRate()));
            descField.setText(planToEdit.getDescription());
        }

        grid.add(new Label("Plan Name: *"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Monthly Rental (₹): *"), 0, 1);
        grid.add(rentalField, 1, 1);
        grid.add(new Label("Free Minutes: *"), 0, 2);
        grid.add(freeMinsField, 1, 2);
        grid.add(new Label("Local Call Rate (₹/min): *"), 0, 3);
        grid.add(localRateField, 1, 3);
        grid.add(new Label("STD Call Rate (₹/min): *"), 0, 4);
        grid.add(stdRateField, 1, 4);
        grid.add(new Label("ISD Call Rate (₹/min): *"), 0, 5);
        grid.add(isdRateField, 1, 5);
        grid.add(new Label("SMS Rate (₹):"), 0, 6);
        grid.add(smsRateField, 1, 6);
        grid.add(new Label("Description:"), 0, 7);
        grid.add(descField, 1, 7);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtnType) {
                String name = nameField.getText();
                if (name == null || name.isBlank()) {
                    AlertUtil.showError(dialog.getDialogPane().getScene().getWindow(), "Validation Error", "Plan name is required.");
                    return null;
                }
                if (!ValidationUtil.isPositiveNumber(rentalField.getText()) ||
                    !ValidationUtil.isPositiveInteger(freeMinsField.getText()) ||
                    !ValidationUtil.isPositiveNumber(localRateField.getText()) ||
                    !ValidationUtil.isPositiveNumber(stdRateField.getText()) ||
                    !ValidationUtil.isPositiveNumber(isdRateField.getText())) {
                    AlertUtil.showError(dialog.getDialogPane().getScene().getWindow(), "Validation Error", "Please provide valid numeric rates.");
                    return null;
                }

                double rental = Double.parseDouble(rentalField.getText().trim());
                int freeMins = Integer.parseInt(freeMinsField.getText().trim());
                double local = Double.parseDouble(localRateField.getText().trim());
                double std = Double.parseDouble(stdRateField.getText().trim());
                double isd = Double.parseDouble(isdRateField.getText().trim());
                double sms = ValidationUtil.isPositiveNumber(smsRateField.getText()) ? Double.parseDouble(smsRateField.getText().trim()) : 0.25;

                if (planToEdit == null) {
                    return new TariffPlan(0, name.trim(), local, std, isd, rental, freeMins, sms, 10.0, descField.getText().trim());
                } else {
                    planToEdit.setName(name.trim());
                    planToEdit.setMonthlyRental(rental);
                    planToEdit.setFreeMinutes(freeMins);
                    planToEdit.setRatePerMinLocal(local);
                    planToEdit.setRatePerMinStd(std);
                    planToEdit.setRatePerMinIsd(isd);
                    planToEdit.setSmsRate(sms);
                    planToEdit.setDescription(descField.getText().trim());
                    return planToEdit;
                }
            }
            return null;
        });

        Optional<TariffPlan> result = dialog.showAndWait();
        result.ifPresent(plan -> {
            try {
                if (planToEdit == null) {
                    planDAO.createPlan(plan);
                    AlertUtil.showInfo(planTable.getScene().getWindow(), "Plan Created", "Tariff plan created successfully.");
                } else {
                    planDAO.updatePlan(plan);
                    AlertUtil.showInfo(planTable.getScene().getWindow(), "Plan Updated", "Tariff plan updated successfully.");
                }
                loadPlans();
            } catch (SQLException e) {
                AlertUtil.showError(planTable.getScene().getWindow(), "Database Error", "Failed to save plan: " + e.getMessage());
            }
        });
    }

    private void handleDeletePlan(TariffPlan plan) {
        try {
            int subs = planDAO.getCustomerCountForPlan(plan.getId());
            if (subs > 0) {
                AlertUtil.showWarning(planTable.getScene().getWindow(), "Cannot Delete Plan",
                        "This plan currently has " + subs + " active subscriber(s). Reassign them to another plan first.");
                return;
            }

            boolean confirm = AlertUtil.showConfirmation(planTable.getScene().getWindow(), "Confirm Plan Deletion",
                    "Delete plan '" + plan.getName() + "'?");
            if (confirm) {
                planDAO.deletePlan(plan.getId());
                loadPlans();
            }
        } catch (SQLException e) {
            AlertUtil.showError(planTable.getScene().getWindow(), "Error", "Could not delete plan: " + e.getMessage());
        }
    }
}
