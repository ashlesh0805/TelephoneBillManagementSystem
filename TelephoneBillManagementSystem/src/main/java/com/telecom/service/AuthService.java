package com.telecom.service;

import com.telecom.dao.UserDAO;
import com.telecom.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service managing user authentication, role checks, and active session state.
 */
public class AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    private static AuthService instance;

    private final UserDAO userDAO;
    private User currentUser;

    private AuthService() {
        this.userDAO = new UserDAO();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * Authenticates a user with username and raw password.
     */
    public boolean login(String username, String password) {
        try {
            User user = userDAO.findByUsername(username);
            if (user == null) {
                return false;
            }

            String computedHash = hashPassword(password, user.getSalt());
            if (computedHash.equalsIgnoreCase(user.getPasswordHash())) {
                this.currentUser = user;
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during user login", e);
        }
        return false;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    /**
     * Computes SHA-256 hash of password + salt.
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest((password + salt).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in JVM", e);
        }
    }
}
