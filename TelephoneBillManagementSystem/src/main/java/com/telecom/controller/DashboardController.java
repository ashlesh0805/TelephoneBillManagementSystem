package com.telecom.controller;

import com.telecom.service.ReportService;
import com.telecom.util.CurrencyUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Operational Dashboard presenting executive KPIs and visual charts.
 */
public class DashboardController {
    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());

    @FXML private Label revenueValueLabel;
    @FXML private Label customersValueLabel;
    @FXML private Label overdueValueLabel;
    @FXML private Label callsValueLabel;

    @FXML private BarChart<String, Number> revenueBarChart;
    @FXML private CategoryAxis chartXAxis;
    @FXML private NumberAxis chartYAxis;

    @FXML private PieChart planPieChart;

    private final ReportService reportService = new ReportService();

    @FXML
    public void initialize() {
        loadMetrics();
        loadCharts();
    }

    private void loadMetrics() {
        try {
            ReportService.DashboardMetrics metrics = reportService.getDashboardMetrics();
            revenueValueLabel.setText(CurrencyUtil.format(metrics.totalRevenueThisMonth));
            customersValueLabel.setText(String.format("%d Active", metrics.activeCustomers));
            overdueValueLabel.setText(String.format("%d Invoices", metrics.overdueBillsCount));
            callsValueLabel.setText(String.format("%d calls (%d mins)", metrics.totalCallsLogged, metrics.totalMinutesLogged));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch dashboard metrics", e);
        }
    }

    private void loadCharts() {
        try {
            // 1. Revenue Bar Chart
            Map<String, Double> trend = reportService.getMonthlyBilledTrend();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Billing Volume (₹)");

            for (Map.Entry<String, Double> entry : trend.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            revenueBarChart.getData().clear();
            revenueBarChart.getData().add(series);
            revenueBarChart.setLegendVisible(false);

            // 2. Plan Distribution Pie Chart
            Map<String, Integer> plans = reportService.getPlanDistribution();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            for (Map.Entry<String, Integer> entry : plans.entrySet()) {
                if (entry.getValue() > 0) {
                    pieData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
                }
            }
            planPieChart.setData(pieData);
            planPieChart.setLegendVisible(true);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load dashboard charts data", e);
        }
    }
}
