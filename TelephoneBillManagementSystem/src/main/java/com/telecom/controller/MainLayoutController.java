package com.telecom.controller;

import com.telecom.MainApp;
import com.telecom.model.User;
import com.telecom.service.AuthService;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the main application shell with sidebar navigation, header status,
 * animated view switching, and theme controls.
 */
public class MainLayoutController {
    private static final Logger LOGGER = Logger.getLogger(MainLayoutController.class.getName());

    @FXML private BorderPane rootBorderPane;
    @FXML private StackPane contentHolder;
    @FXML private Label pageTitleLabel;
    @FXML private Label pageSubtitleLabel;
    @FXML private Label currentDateLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleBadge;
    @FXML private Button themeToggleButton;

    @FXML private Button navDashboard;
    @FXML private Button navCustomers;
    @FXML private Button navCalls;
    @FXML private Button navBills;
    @FXML private Button navPlans;
    @FXML private Button navReports;

    private final AuthService authService = AuthService.getInstance();
    private Button currentActiveNav;

    @FXML
    public void initialize() {
        // Setup user details in sidebar
        User user = authService.getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getFullName());
            userRoleBadge.setText(user.getRole());
            if (user.isAdmin()) {
                userRoleBadge.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8; -fx-padding: 2px 8px; -fx-background-radius: 10px; -fx-font-weight: bold; -fx-font-size: 10.5px;");
            } else {
                userRoleBadge.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-padding: 2px 8px; -fx-background-radius: 10px; -fx-font-weight: bold; -fx-font-size: 10.5px;");
            }
        }

        // Set date
        currentDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));

        // Update theme toggle text
        updateThemeButtonLabel();

        // Default to Dashboard
        showDashboard();
    }

    @FXML
    public void showDashboard() {
        setActiveNav(navDashboard);
        loadView("/fxml/dashboard.fxml", "Operational Dashboard", "Real-time metrics, revenue performance, and key indicators");
    }

    @FXML
    public void showCustomers() {
        setActiveNav(navCustomers);
        loadView("/fxml/customers.fxml", "Subscriber Management", "Manage telecom accounts, subscriptions, and active lines");
    }

    @FXML
    public void showCallRecords() {
        setActiveNav(navCalls);
        loadView("/fxml/call_records.fxml", "Call Detail Records (CDR)", "Log calls, auto-detect categories, or bulk import CSV records");
    }

    @FXML
    public void showBills() {
        setActiveNav(navBills);
        loadView("/fxml/bills.fxml", "Billing & Invoicing", "Generate monthly bills, view itemized statements, and export PDFs");
    }

    @FXML
    public void showTariffPlans() {
        setActiveNav(navPlans);
        loadView("/fxml/tariff_plans.fxml", "Tariff Plans & Rates", "Configure local, STD, ISD calling rates and monthly allowances");
    }

    @FXML
    public void showReports() {
        setActiveNav(navReports);
        loadView("/fxml/reports.fxml", "Executive Analytics & Reports", "Financial statements, top callers leaderboard, and CSV exports");
    }

    @FXML
    private void handleToggleTheme() {
        MainApp.toggleDarkMode();
        updateThemeButtonLabel();
    }

    private void updateThemeButtonLabel() {
        if (MainApp.isDarkMode()) {
            themeToggleButton.setText("☀️ Light Mode");
        } else {
            themeToggleButton.setText("🌙 Dark Mode");
        }
    }

    @FXML
    private void handleLogout() {
        authService.logout();
        MainApp.showLoginView();
    }

    private void setActiveNav(Button btn) {
        if (currentActiveNav != null) {
            currentActiveNav.getStyleClass().remove("nav-btn-active");
        }
        currentActiveNav = btn;
        if (currentActiveNav != null && !currentActiveNav.getStyleClass().contains("nav-btn-active")) {
            currentActiveNav.getStyleClass().add("nav-btn-active");
        }
    }

    private void loadView(String fxmlPath, String title, String subtitle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            pageTitleLabel.setText(title);
            pageSubtitleLabel.setText(subtitle);

            // Smooth fade transition
            view.setOpacity(0.0);
            contentHolder.getChildren().clear();
            contentHolder.getChildren().add(view);

            FadeTransition ft = new FadeTransition(Duration.millis(250), view);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load view: " + fxmlPath, e);
        }
    }
}
