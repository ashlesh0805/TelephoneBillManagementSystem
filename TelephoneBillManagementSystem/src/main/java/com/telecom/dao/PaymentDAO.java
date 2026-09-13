package com.telecom.dao;

import com.telecom.model.Payment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Customer invoice payments.
 */
public class PaymentDAO {
    private final DatabaseManager dbManager;

    public PaymentDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public List<Payment> getAllPayments() throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.*, b.bill_number, c.name as customer_name " +
                "FROM payments p " +
                "JOIN bills b ON p.bill_id = b.id " +
                "JOIN customers c ON b.customer_id = c.id " +
                "ORDER BY p.id DESC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToPayment(rs));
            }
        }
        return list;
    }

    public List<Payment> getPaymentsByBillId(int billId) throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.*, b.bill_number, c.name as customer_name " +
                "FROM payments p " +
                "JOIN bills b ON p.bill_id = b.id " +
                "JOIN customers c ON b.customer_id = c.id " +
                "WHERE p.bill_id = ? " +
                "ORDER BY p.id ASC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, billId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPayment(rs));
                }
            }
        }
        return list;
    }

    public double getTotalPaidForBill(int billId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount_paid), 0) FROM payments WHERE bill_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, billId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    public boolean recordPayment(Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (bill_id, amount_paid, payment_date, payment_mode, " +
                "transaction_reference, notes) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, payment.getBillId());
            stmt.setDouble(2, payment.getAmountPaid());
            stmt.setString(3, payment.getPaymentDate());
            stmt.setString(4, payment.getPaymentMode());
            stmt.setString(5, payment.getTransactionReference());
            stmt.setString(6, payment.getNotes());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        payment.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("id"));
        p.setBillId(rs.getInt("bill_id"));
        p.setBillNumber(rs.getString("bill_number"));
        p.setCustomerName(rs.getString("customer_name"));
        p.setAmountPaid(rs.getDouble("amount_paid"));
        p.setPaymentDate(rs.getString("payment_date"));
        p.setPaymentMode(rs.getString("payment_mode"));
        p.setTransactionReference(rs.getString("transaction_reference"));
        p.setNotes(rs.getString("notes"));
        return p;
    }
}
