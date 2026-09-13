package com.telecom.dao;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages SQLite database connectivity, schema bootstrapping, and seed data initialization.
 */
public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DB_URL = "jdbc:sqlite:telecom.db";
    private static DatabaseManager instance;

    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "SQLite JDBC Driver not found in classpath!", e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Initializes database tables and default sample data if not already present.
     */
    public synchronized void initializeDatabase() {
        try (Connection conn = getConnection()) {
            // 1. Execute schema.sql
            executeScript(conn, "/database/schema.sql");

            // 2. Check if users table is populated; if not, execute seed_data.sql
            try (Statement stmt = conn.createStatement();
                 var rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    LOGGER.info("Empty database detected. Populating with initial seed data...");
                    executeScript(conn, "/database/seed_data.sql");
                    LOGGER.info("Database successfully seeded with demo users, plans, and call records.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error initializing SQLite database schema: " + e.getMessage(), e);
        }
    }

    /**
     * Executes a SQL script file from the classpath resources.
     */
    private void executeScript(Connection conn, String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                LOGGER.warning("Database resource script not found: " + resourcePath);
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sql = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                        continue;
                    }
                    sql.append(line).append("\n");
                    if (trimmed.endsWith(";")) {
                        try (Statement stmt = conn.createStatement()) {
                            stmt.execute(sql.toString());
                        }
                        sql.setLength(0);
                    }
                }
                // Flush remaining buffer if any
                if (!sql.toString().trim().isEmpty()) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(sql.toString());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error executing SQL script: " + resourcePath, e);
        }
    }
}
