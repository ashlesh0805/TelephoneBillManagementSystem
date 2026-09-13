package com.telecom.dao;

import com.telecom.model.TariffPlan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Tariff Plans management.
 */
public class TariffPlanDAO {
    private final DatabaseManager dbManager;

    public TariffPlanDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public List<TariffPlan> getAllPlans() throws SQLException {
        List<TariffPlan> list = new ArrayList<>();
        String sql = "SELECT * FROM tariff_plans ORDER BY id ASC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToPlan(rs));
            }
        }
        return list;
    }

    public TariffPlan getPlanById(int id) throws SQLException {
        String sql = "SELECT * FROM tariff_plans WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPlan(rs);
                }
            }
        }
        return null;
    }

    public boolean createPlan(TariffPlan plan) throws SQLException {
        String sql = "INSERT INTO tariff_plans (name, rate_per_min_local, rate_per_min_std, rate_per_min_isd, " +
                "monthly_rental, free_minutes, sms_rate, data_rate, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, plan.getName());
            stmt.setDouble(2, plan.getRatePerMinLocal());
            stmt.setDouble(3, plan.getRatePerMinStd());
            stmt.setDouble(4, plan.getRatePerMinIsd());
            stmt.setDouble(5, plan.getMonthlyRental());
            stmt.setInt(6, plan.getFreeMinutes());
            stmt.setDouble(7, plan.getSmsRate());
            stmt.setDouble(8, plan.getDataRate());
            stmt.setString(9, plan.getDescription());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        plan.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updatePlan(TariffPlan plan) throws SQLException {
        String sql = "UPDATE tariff_plans SET name = ?, rate_per_min_local = ?, rate_per_min_std = ?, " +
                "rate_per_min_isd = ?, monthly_rental = ?, free_minutes = ?, sms_rate = ?, data_rate = ?, " +
                "description = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, plan.getName());
            stmt.setDouble(2, plan.getRatePerMinLocal());
            stmt.setDouble(3, plan.getRatePerMinStd());
            stmt.setDouble(4, plan.getRatePerMinIsd());
            stmt.setDouble(5, plan.getMonthlyRental());
            stmt.setInt(6, plan.getFreeMinutes());
            stmt.setDouble(7, plan.getSmsRate());
            stmt.setDouble(8, plan.getDataRate());
            stmt.setString(9, plan.getDescription());
            stmt.setInt(10, plan.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deletePlan(int id) throws SQLException {
        String sql = "DELETE FROM tariff_plans WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public int getCustomerCountForPlan(int planId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers WHERE plan_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, planId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private TariffPlan mapResultSetToPlan(ResultSet rs) throws SQLException {
        TariffPlan plan = new TariffPlan();
        plan.setId(rs.getInt("id"));
        plan.setName(rs.getString("name"));
        plan.setRatePerMinLocal(rs.getDouble("rate_per_min_local"));
        plan.setRatePerMinStd(rs.getDouble("rate_per_min_std"));
        plan.setRatePerMinIsd(rs.getDouble("rate_per_min_isd"));
        plan.setMonthlyRental(rs.getDouble("monthly_rental"));
        plan.setFreeMinutes(rs.getInt("free_minutes"));
        plan.setSmsRate(rs.getDouble("sms_rate"));
        plan.setDataRate(rs.getDouble("data_rate"));
        plan.setDescription(rs.getString("description"));
        return plan;
    }
}
