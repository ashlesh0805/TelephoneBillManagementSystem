package com.telecom.dao;

import com.telecom.model.Bill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Bills, Invoices, and Revenue Statistics.
 */
public class BillDAO {
    private final DatabaseManager dbManager;

    public BillDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public List<Bill> getAllBills() throws SQLException {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT b.*, c.name as customer_name, c.phone_number as customer_phone, " +
                "c.address as customer_address, p.name as plan_name " +
                "FROM bills b " +
                "JOIN customers c ON b.customer_id = c.id " +
                "LEFT JOIN tariff_plans p ON c.plan_id = p.id " +
                "ORDER BY b.id DESC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToBill(rs));
            }
        }
        return list;
    }

    public Bill getBillById(int id) throws SQLException {
        String sql = "SELECT b.*, c.name as customer_name, c.phone_number as customer_phone, " +
                "c.address as customer_address, p.name as plan_name " +
                "FROM bills b " +
                "JOIN customers c ON b.customer_id = c.id " +
                "LEFT JOIN tariff_plans p ON c.plan_id = p.id " +
                "WHERE b.id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBill(rs);
                }
            }
        }
        return null;
    }

    public Bill getBillByNumber(String billNumber) throws SQLException {
        String sql = "SELECT b.*, c.name as customer_name, c.phone_number as customer_phone, " +
                "c.address as customer_address, p.name as plan_name " +
                "FROM bills b " +
                "JOIN customers c ON b.customer_id = c.id " +
                "LEFT JOIN tariff_plans p ON c.plan_id = p.id " +
                "WHERE b.bill_number = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, billNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBill(rs);
                }
            }
        }
        return null;
    }

    public Bill getBillByCustomerAndPeriod(int customerId, int month, int year) throws SQLException {
        String sql = "SELECT b.*, c.name as customer_name, c.phone_number as customer_phone, " +
                "c.address as customer_address, p.name as plan_name " +
                "FROM bills b " +
                "JOIN customers c ON b.customer_id = c.id " +
                "LEFT JOIN tariff_plans p ON c.plan_id = p.id " +
                "WHERE b.customer_id = ? AND b.billing_month = ? AND b.billing_year = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, month);
            stmt.setInt(3, year);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBill(rs);
                }
            }
        }
        return null;
    }

    public boolean createBill(Bill bill) throws SQLException {
        String sql = "INSERT INTO bills (bill_number, customer_id, billing_month, billing_year, " +
                "total_call_charges, rental_charges, tax_percentage, tax_amount, discount_amount, " +
                "late_fee, total_amount, due_date, payment_status, generated_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, bill.getBillNumber());
            stmt.setInt(2, bill.getCustomerId());
            stmt.setInt(3, bill.getBillingMonth());
            stmt.setInt(4, bill.getBillingYear());
            stmt.setDouble(5, bill.getTotalCallCharges());
            stmt.setDouble(6, bill.getRentalCharges());
            stmt.setDouble(7, bill.getTaxPercentage());
            stmt.setDouble(8, bill.getTaxAmount());
            stmt.setDouble(9, bill.getDiscountAmount());
            stmt.setDouble(10, bill.getLateFee());
            stmt.setDouble(11, bill.getTotalAmount());
            stmt.setString(12, bill.getDueDate());
            stmt.setString(13, bill.getPaymentStatus());
            stmt.setString(14, bill.getGeneratedDate());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        bill.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updatePaymentStatus(int billId, String newStatus) throws SQLException {
        String sql = "UPDATE bills SET payment_status = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, billId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteBill(int id) throws SQLException {
        String sql = "DELETE FROM bills WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public int getOverdueBillsCount() throws SQLException {
        // Automatically check date against current date if unpaid
        String today = LocalDate.now().toString();
        String sql = "SELECT COUNT(*) FROM bills WHERE payment_status = 'OVERDUE' " +
                "OR (payment_status = 'UNPAID' AND due_date < ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, today);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public double getTotalRevenueThisMonth(int month, int year) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount_paid), 0) FROM payments " +
                "WHERE payment_date LIKE ?";
        String prefix = String.format("%04d-%02d", year, month);
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, prefix + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    public double getTotalBilledAmount() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM bills";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public double getTotalCollectedAmount() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount_paid), 0) FROM payments";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    /**
     * Returns monthly billing amount trend for the past 6 months.
     */
    public Map<String, Double> getMonthlyBilledTrend() throws SQLException {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = "SELECT printf('%04d-%02d', billing_year, billing_month) as period, " +
                "SUM(total_amount) as total FROM bills " +
                "GROUP BY billing_year, billing_month " +
                "ORDER BY billing_year ASC, billing_month ASC LIMIT 6";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("period"), rs.getDouble("total"));
            }
        }
        return map;
    }

    /**
     * Plan-wise customer distribution.
     */
    public Map<String, Integer> getPlanDistribution() throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT p.name, COUNT(c.id) as count FROM tariff_plans p " +
                "LEFT JOIN customers c ON p.id = c.plan_id " +
                "GROUP BY p.id, p.name";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("name"), rs.getInt("count"));
            }
        }
        return map;
    }

    private Bill mapResultSetToBill(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setId(rs.getInt("id"));
        b.setBillNumber(rs.getString("bill_number"));
        b.setCustomerId(rs.getInt("customer_id"));
        b.setCustomerName(rs.getString("customer_name"));
        b.setCustomerPhone(rs.getString("customer_phone"));
        b.setCustomerAddress(rs.getString("customer_address"));
        b.setPlanName(rs.getString("plan_name"));
        b.setBillingMonth(rs.getInt("billing_month"));
        b.setBillingYear(rs.getInt("billing_year"));
        b.setTotalCallCharges(rs.getDouble("total_call_charges"));
        b.setRentalCharges(rs.getDouble("rental_charges"));
        b.setTaxPercentage(rs.getDouble("tax_percentage"));
        b.setTaxAmount(rs.getDouble("tax_amount"));
        b.setDiscountAmount(rs.getDouble("discount_amount"));
        b.setLateFee(rs.getDouble("late_fee"));
        b.setTotalAmount(rs.getDouble("total_amount"));
        b.setDueDate(rs.getString("due_date"));
        b.setPaymentStatus(rs.getString("payment_status"));
        b.setGeneratedDate(rs.getString("generated_date"));
        return b;
    }
}
