package com.telecom;

import com.telecom.dao.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main JavaFX Application class managing stage lifecycle, views, and theme switching.
 */
public class MainApp extends Application {
    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());
    private static Stage primaryStage;
    private static boolean isDarkMode = false;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Apex Telecom - Telephone Bill Management System");

        // Initialize SQLite Database asynchronously or on bootstrap
        DatabaseManager.getInstance();

        showLoginView();
    }

    public static void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 950, 620);
            applyTheme(scene);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load login view FXML", e);
        }
    }

    public static void showMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/main_layout.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 760);
            applyTheme(scene);
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(1050);
            primaryStage.setMinHeight(680);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load main layout FXML", e);
        }
    }

    public static void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        if (primaryStage != null && primaryStage.getScene() != null) {
            applyTheme(primaryStage.getScene());
        }
    }

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    public static void applyTheme(Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(MainApp.class.getResource("/css/styles.css")).toExternalForm());
        if (isDarkMode) {
            scene.getStylesheets().add(Objects.requireNonNull(MainApp.class.getResource("/css/dark_theme.css")).toExternalForm());
        } else {
            scene.getStylesheets().add(Objects.requireNonNull(MainApp.class.getResource("/css/light_theme.css")).toExternalForm());
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
