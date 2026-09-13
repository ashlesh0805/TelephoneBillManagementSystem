package com.telecom.dao;

import com.telecom.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Customer lifecycle and lookup operations.
 */
public class CustomerDAO {
    private final DatabaseManager dbManager;

    public CustomerDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT c.*, p.name as plan_name FROM customers c " +
                "LEFT JOIN tariff_plans p ON c.plan_id = p.id " +
                "ORDER BY c.id DESC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToCustomer(rs));
            }
        }
        return list;
    }

    public Customer getCustomerById(int id) throws SQLException {
        String sql = "SELECT c.*, p.name as plan_name FROM customers c " +
                "LEFT JOIN tariff_plans p ON c.plan_id = p.id " +
                "WHERE c.id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        }
        return null;
    }

    public Customer getCustomerByPhone(String phone) throws SQLException {
        String sql = "SELECT c.*, p.name as plan_name FROM customers c " +
                "LEFT JOIN tariff_plans p ON c.plan_id = p.id " +
                "WHERE c.phone_number = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        }
        return null;
    }

    public boolean createCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (name, phone_number, email, address, connection_date, plan_id, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getPhoneNumber());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getAddress());
            stmt.setString(5, customer.getConnectionDate());
            stmt.setInt(6, customer.getPlanId());
            stmt.setString(7, customer.getStatus());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        customer.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateCustomer(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET name = ?, phone_number = ?, email = ?, address = ?, " +
                "connection_date = ?, plan_id = ?, status = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getPhoneNumber());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getAddress());
            stmt.setString(5, customer.getConnectionDate());
            stmt.setInt(6, customer.getPlanId());
            stmt.setString(7, customer.getStatus());
            stmt.setInt(8, customer.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteCustomer(int id) throws SQLException {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Customer> searchCustomers(String keyword, String statusFilter) throws SQLException {
        List<Customer> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT c.*, p.name as plan_name FROM customers c " +
                "LEFT JOIN tariff_plans p ON c.plan_id = p.id WHERE 1=1 "
        );

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (c.name LIKE ? OR c.phone_number LIKE ? OR c.email LIKE ?) ");
        }

        if (statusFilter != null && !statusFilter.equalsIgnoreCase("ALL")) {
            sql.append("AND c.status = ? ");
        }

        sql.append("ORDER BY c.id DESC");

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim() + "%";
                stmt.setString(paramIndex++, pattern);
                stmt.setString(paramIndex++, pattern);
                stmt.setString(paramIndex++, pattern);
            }
            if (statusFilter != null && !statusFilter.equalsIgnoreCase("ALL")) {
                stmt.setString(paramIndex++, statusFilter.toUpperCase());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCustomer(rs));
                }
            }
        }
        return list;
    }

    public int getActiveCustomerCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers WHERE status = 'ACTIVE'";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getTotalCustomerCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setPhoneNumber(rs.getString("phone_number"));
        c.setEmail(rs.getString("email"));
        c.setAddress(rs.getString("address"));
        c.setConnectionDate(rs.getString("connection_date"));
        c.setPlanId(rs.getInt("plan_id"));
        c.setPlanName(rs.getString("plan_name"));
        c.setStatus(rs.getString("status"));
        return c;
    }
}
