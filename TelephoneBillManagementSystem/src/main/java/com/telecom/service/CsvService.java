package com.telecom.service;

import com.telecom.dao.CallRecordDAO;
import com.telecom.dao.CustomerDAO;
import com.telecom.dao.TariffPlanDAO;
import com.telecom.model.Bill;
import com.telecom.model.CallRecord;
import com.telecom.model.CallType;
import com.telecom.model.Customer;
import com.telecom.model.TariffPlan;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to handle CSV operations: bulk importing call logs and exporting reports.
 */
public class CsvService {
    private final CustomerDAO customerDAO;
    private final TariffPlanDAO planDAO;
    private final CallRecordDAO callDAO;
    private final BillingEngine billingEngine;

    public CsvService() {
        this.customerDAO = new CustomerDAO();
        this.planDAO = new TariffPlanDAO();
        this.callDAO = new CallRecordDAO();
        this.billingEngine = new BillingEngine();
    }

    /**
     * Bulk imports call records for a given customer from a CSV file.
     * Expected CSV format:
     * timestamp,destination,duration_seconds
     * e.g. 2026-08-01 10:15:30,+919820055112,180
     */
    public int importCallRecordsCsv(int customerId, File csvFile) throws Exception {
        Customer customer = customerDAO.getCustomerById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer with ID " + customerId + " not found.");
        }

        TariffPlan plan = planDAO.getPlanById(customer.getPlanId());
        if (plan == null) {
            throw new IllegalStateException("Tariff plan not found for customer.");
        }

        List<CallRecord> calls = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Check header
                if (isFirstLine) {
                    isFirstLine = false;
                    if (line.toLowerCase().contains("timestamp") || line.toLowerCase().contains("destination")) {
                        continue;
                    }
                }

                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String timestamp = parts[0].trim();
                    String destination = parts[1].trim();
                    int duration = Integer.parseInt(parts[2].trim());

                    CallType type = CallService.detectCallType(destination);
                    double cost = billingEngine.computeCallCost(type, duration, plan);

                    CallRecord call = new CallRecord();
                    call.setCustomerId(customerId);
                    call.setCallTimestamp(timestamp);
                    call.setDestinationNumber(destination);
                    call.setCallType(type);
                    call.setDurationSeconds(duration);
                    call.setComputedCost(cost);

                    calls.add(call);
                }
            }
        }

        if (!calls.isEmpty()) {
            return callDAO.bulkInsertCalls(calls);
        }
        return 0;
    }

    /**
     * Exports a list of bills to CSV.
     */
    public void exportBillsToCsv(List<Bill> bills, File targetFile) throws IOException {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {
            pw.println("Bill Number,Customer Name,Phone,Period,Rental (INR),Call Charges (INR),Tax (INR),Late Fee (INR),Discount (INR),Total Amount (INR),Due Date,Status");
            for (Bill b : bills) {
                pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,\"%s\",\"%s\"%n",
                        b.getBillNumber(),
                        b.getCustomerName(),
                        b.getCustomerPhone(),
                        b.getBillingPeriod(),
                        b.getRentalCharges(),
                        b.getTotalCallCharges(),
                        b.getTaxAmount(),
                        b.getLateFee(),
                        b.getDiscountAmount(),
                        b.getTotalAmount(),
                        b.getDueDate(),
                        b.getPaymentStatus());
            }
        }
    }

    /**
     * Exports top callers report to CSV.
     */
    public void exportTopCallersToCsv(List<ReportService.CallerStat> stats, File targetFile) throws IOException {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {
            pw.println("Customer Name,Phone Number,Tariff Plan,Total Calls,Duration (Seconds),Formatted Duration,Total Spend (INR)");
            for (ReportService.CallerStat s : stats) {
                pw.printf("\"%s\",\"%s\",\"%s\",%d,%d,\"%s\",%.2f%n",
                        s.customerName,
                        s.phoneNumber,
                        s.planName,
                        s.callCount,
                        s.totalDurationSeconds,
                        s.getFormattedDuration(),
                        s.totalSpent);
            }
        }
    }
}
