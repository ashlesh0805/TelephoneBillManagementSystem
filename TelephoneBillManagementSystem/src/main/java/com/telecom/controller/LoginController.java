package com.telecom.controller;

import com.telecom.MainApp;
import com.telecom.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for Login view supporting authentication and demo credentials.
 */
public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = AuthService.getInstance();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            showError("Please enter both username and password.");
            return;
        }

        boolean success = authService.login(username.trim(), password.trim());
        if (success) {
            errorLabel.setVisible(false);
            MainApp.showMainView();
        } else {
            showError("Invalid credentials. Check username or password.");
        }
    }

    @FXML
    private void handleFillAdmin() {
        usernameField.setText("admin");
        passwordField.setText("admin123");
        errorLabel.setVisible(false);
    }

    @FXML
    private void handleFillStaff() {
        usernameField.setText("staff");
        passwordField.setText("staff123");
        errorLabel.setVisible(false);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
