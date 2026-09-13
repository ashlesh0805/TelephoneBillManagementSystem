package com.telecom.service;

import com.telecom.dao.BillDAO;
import com.telecom.dao.CallRecordDAO;
import com.telecom.dao.CustomerDAO;
import com.telecom.dao.DatabaseManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for aggregating analytics, executive dashboard metrics, and revenue reports.
 */
public class ReportService {
    private final CustomerDAO customerDAO;
    private final CallRecordDAO callDAO;
    private final BillDAO billDAO;
    private final DatabaseManager dbManager;

    public static class DashboardMetrics {
        public double totalRevenueThisMonth;
        public double totalBilledEver;
        public double totalCollectedEver;
        public int activeCustomers;
        public int totalCustomers;
        public int overdueBillsCount;
        public int totalCallsLogged;
        public long totalMinutesLogged;
    }

    public static class CallerStat {
        public String customerName;
        public String phoneNumber;
        public String planName;
        public int callCount;
        public int totalDurationSeconds;
        public double totalSpent;

        public String getFormattedDuration() {
            int mins = totalDurationSeconds / 60;
            int secs = totalDurationSeconds % 60;
            return String.format("%d mins %02d secs", mins, secs);
        }
    }

    public ReportService() {
        this.customerDAO = new CustomerDAO();
        this.callDAO = new CallRecordDAO();
        this.billDAO = new BillDAO();
        this.dbManager = DatabaseManager.getInstance();
    }

    public DashboardMetrics getDashboardMetrics() throws SQLException {
        DashboardMetrics metrics = new DashboardMetrics();
        LocalDate now = LocalDate.now();

        metrics.totalRevenueThisMonth = billDAO.getTotalRevenueThisMonth(now.getMonthValue(), now.getYear());
        metrics.totalBilledEver = billDAO.getTotalBilledAmount();
        metrics.totalCollectedEver = billDAO.getTotalCollectedAmount();
        metrics.activeCustomers = customerDAO.getActiveCustomerCount();
        metrics.totalCustomers = customerDAO.getTotalCustomerCount();
        metrics.overdueBillsCount = billDAO.getOverdueBillsCount();
        metrics.totalCallsLogged = callDAO.getTotalCallsCount();
        metrics.totalMinutesLogged = (long) Math.ceil(callDAO.getTotalBilledDurationSeconds() / 60.0);

        return metrics;
    }

    public Map<String, Double> getMonthlyBilledTrend() throws SQLException {
        return billDAO.getMonthlyBilledTrend();
    }

    public Map<String, Integer> getPlanDistribution() throws SQLException {
        return billDAO.getPlanDistribution();
    }

    public List<CallerStat> getTopCallers(int limit) throws SQLException {
        List<CallerStat> stats = new ArrayList<>();
        String sql = "SELECT c.name as customer_name, c.phone_number, p.name as plan_name, " +
                "COUNT(cr.id) as call_count, " +
                "COALESCE(SUM(cr.duration_seconds), 0) as total_duration, " +
                "COALESCE(SUM(cr.computed_cost), 0) as total_spent " +
                "FROM customers c " +
                "LEFT JOIN tariff_plans p ON c.plan_id = p.id " +
                "LEFT JOIN call_records cr ON c.id = cr.customer_id " +
                "GROUP BY c.id " +
                "ORDER BY total_duration DESC " +
                "LIMIT " + limit;

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                CallerStat stat = new CallerStat();
                stat.customerName = rs.getString("customer_name");
                stat.phoneNumber = rs.getString("phone_number");
                stat.planName = rs.getString("plan_name");
                stat.callCount = rs.getInt("call_count");
                stat.totalDurationSeconds = rs.getInt("total_duration");
                stat.totalSpent = rs.getDouble("total_spent");
                stats.add(stat);
            }
        }
        return stats;
    }
}
