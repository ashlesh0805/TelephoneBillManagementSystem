package com.telecom.dao;

import com.telecom.model.CallRecord;
import com.telecom.model.CallType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Call Detail Records (CDR).
 */
public class CallRecordDAO {
    private final DatabaseManager dbManager;

    public CallRecordDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public List<CallRecord> getAllCalls() throws SQLException {
        List<CallRecord> list = new ArrayList<>();
        String sql = "SELECT cr.*, c.name as customer_name, c.phone_number as customer_phone " +
                "FROM call_records cr " +
                "JOIN customers c ON cr.customer_id = c.id " +
                "ORDER BY cr.call_timestamp DESC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToCall(rs));
            }
        }
        return list;
    }

    public List<CallRecord> getCallsByCustomer(int customerId) throws SQLException {
        List<CallRecord> list = new ArrayList<>();
        String sql = "SELECT cr.*, c.name as customer_name, c.phone_number as customer_phone " +
                "FROM call_records cr " +
                "JOIN customers c ON cr.customer_id = c.id " +
                "WHERE cr.customer_id = ? " +
                "ORDER BY cr.call_timestamp DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCall(rs));
                }
            }
        }
        return list;
    }

    public List<CallRecord> getCallsByCustomerAndPeriod(int customerId, int month, int year) throws SQLException {
        List<CallRecord> list = new ArrayList<>();
        // SQLite date formatting: e.g. '2026-08%' for month 8, year 2026
        String prefix = String.format("%04d-%02d", year, month);
        String sql = "SELECT cr.*, c.name as customer_name, c.phone_number as customer_phone " +
                "FROM call_records cr " +
                "JOIN customers c ON cr.customer_id = c.id " +
                "WHERE cr.customer_id = ? AND cr.call_timestamp LIKE ? " +
                "ORDER BY cr.call_timestamp ASC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.setString(2, prefix + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCall(rs));
                }
            }
        }
        return list;
    }

    public boolean createCall(CallRecord call) throws SQLException {
        String sql = "INSERT INTO call_records (customer_id, call_timestamp, destination_number, call_type, " +
                "duration_seconds, computed_cost) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, call.getCustomerId());
            stmt.setString(2, call.getCallTimestamp());
            stmt.setString(3, call.getDestinationNumber());
            stmt.setString(4, call.getCallType().name());
            stmt.setInt(5, call.getDurationSeconds());
            stmt.setDouble(6, call.getComputedCost());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        call.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public int bulkInsertCalls(List<CallRecord> calls) throws SQLException {
        String sql = "INSERT INTO call_records (customer_id, call_timestamp, destination_number, call_type, " +
                "duration_seconds, computed_cost) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (CallRecord call : calls) {
                    stmt.setInt(1, call.getCustomerId());
                    stmt.setString(2, call.getCallTimestamp());
                    stmt.setString(3, call.getDestinationNumber());
                    stmt.setString(4, call.getCallType().name());
                    stmt.setInt(5, call.getDurationSeconds());
                    stmt.setDouble(6, call.getComputedCost());
                    stmt.addBatch();
                }
                int[] results = stmt.executeBatch();
                conn.commit();
                return results.length;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean deleteCall(int id) throws SQLException {
        String sql = "DELETE FROM call_records WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public int getTotalCallsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM call_records";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public long getTotalBilledDurationSeconds() throws SQLException {
        String sql = "SELECT COALESCE(SUM(duration_seconds), 0) FROM call_records";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }

    private CallRecord mapResultSetToCall(ResultSet rs) throws SQLException {
        CallRecord call = new CallRecord();
        call.setId(rs.getInt("id"));
        call.setCustomerId(rs.getInt("customer_id"));
        call.setCustomerName(rs.getString("customer_name"));
        call.setCustomerPhone(rs.getString("customer_phone"));
        call.setCallTimestamp(rs.getString("call_timestamp"));
        call.setDestinationNumber(rs.getString("destination_number"));
        call.setCallType(CallType.fromString(rs.getString("call_type")));
        call.setDurationSeconds(rs.getInt("duration_seconds"));
        call.setComputedCost(rs.getDouble("computed_cost"));
        return call;
    }
}
