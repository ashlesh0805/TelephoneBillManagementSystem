package com.telecom.controller;

import com.telecom.service.CsvService;
import com.telecom.service.ReportService;
import com.telecom.util.AlertUtil;
import com.telecom.util.CurrencyUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Financial and Usage Analytics, Top Callers Leaderboard, and Report CSV exports.
 */
public class ReportsController {
    private static final Logger LOGGER = Logger.getLogger(ReportsController.class.getName());

    @FXML private Label totalBilledLabel;
    @FXML private Label totalCollectedLabel;
    @FXML private Label outstandingLabel;
    @FXML private Label activeSubsLabel;

    @FXML private TableView<ReportService.CallerStat> topCallersTable;
    @FXML private TableColumn<ReportService.CallerStat, String> colCallerName;
    @FXML private TableColumn<ReportService.CallerStat, String> colCallerPhone;
    @FXML private TableColumn<ReportService.CallerStat, String> colCallerPlan;
    @FXML private TableColumn<ReportService.CallerStat, Integer> colCallerCalls;
    @FXML private TableColumn<ReportService.CallerStat, String> colCallerDuration;
    @FXML private TableColumn<ReportService.CallerStat, String> colCallerSpent;

    private final ReportService reportService = new ReportService();
    private final CsvService csvService = new CsvService();
    private final ObservableList<ReportService.CallerStat> callerList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadReportData();
    }

    private void setupTable() {
        colCallerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colCallerPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colCallerPlan.setCellValueFactory(new PropertyValueFactory<>("planName"));
        colCallerCalls.setCellValueFactory(new PropertyValueFactory<>("callCount"));
        colCallerDuration.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedDuration()));
        colCallerSpent.setCellValueFactory(cellData ->
                new SimpleStringProperty(CurrencyUtil.format(cellData.getValue().totalSpent)));

        topCallersTable.setItems(callerList);
    }

    public void loadReportData() {
        try {
            ReportService.DashboardMetrics m = reportService.getDashboardMetrics();
            totalBilledLabel.setText(CurrencyUtil.format(m.totalBilledEver));
            totalCollectedLabel.setText(CurrencyUtil.format(m.totalCollectedEver));
            double outstanding = Math.max(0.0, m.totalBilledEver - m.totalCollectedEver);
            outstandingLabel.setText(CurrencyUtil.format(outstanding));
            activeSubsLabel.setText(m.activeCustomers + " Subscribers");

            List<ReportService.CallerStat> stats = reportService.getTopCallers(15);
            callerList.setAll(stats);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load report analytics", e);
        }
    }

    @FXML
    private void handleExportTopCallersCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Top Callers Report CSV");
        fileChooser.setInitialFileName("top_callers_report.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));

        File file = fileChooser.showSaveDialog(topCallersTable.getScene().getWindow());
        if (file != null) {
            try {
                csvService.exportTopCallersToCsv(callerList, file);
                AlertUtil.showInfo(topCallersTable.getScene().getWindow(), "Export Complete",
                        "Top callers report successfully exported to:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                AlertUtil.showError(topCallersTable.getScene().getWindow(), "Export Failed", e.getMessage());
            }
        }
    }
}
