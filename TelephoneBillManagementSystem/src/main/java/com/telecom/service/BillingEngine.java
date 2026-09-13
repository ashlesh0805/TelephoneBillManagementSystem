package com.telecom.service;

import com.telecom.model.Bill;
import com.telecom.model.CallRecord;
import com.telecom.model.CallType;
import com.telecom.model.Customer;
import com.telecom.model.TariffPlan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Core Billing Engine responsible for computing call charges, free minutes allowances,
 * monthly rentals, GST/taxes, discounts, and final payable amounts.
 */
public class BillingEngine {
    public static final double DEFAULT_TAX_PERCENTAGE = 18.0; // 18% GST (9% CGST + 9% SGST)
    public static final double DEFAULT_LATE_FEE = 50.0;

    /**
     * Data Transfer Object representing intermediate billing charge breakdowns.
     */
    public static class BillingBreakdown {
        public double localCharges;
        public double stdCharges;
        public double isdCharges;
        public double totalCallCharges;
        public int totalCallSeconds;
        public int totalCallMinutes;
        public int freeMinutesUsed;
        public int freeMinutesRemaining;
        public double rentalCharges;
        public double taxAmount;
        public double discountAmount;
        public double lateFee;
        public double totalPayable;
    }

    /**
     * Calculates the itemized billing breakdown for a customer in a specific billing period.
     *
     * @param plan Tariff plan subscribed by the customer
     * @param calls Call records belonging to the billing period
     * @param discount Optional discount in rupees
     * @param lateFee Optional late fee (if overdue)
     * @return Itemized breakdown
     */
    public BillingBreakdown computeBillBreakdown(TariffPlan plan, List<CallRecord> calls, double discount, double lateFee) {
        BillingBreakdown breakdown = new BillingBreakdown();
        breakdown.rentalCharges = round2(plan.getMonthlyRental());
        breakdown.discountAmount = round2(discount);
        breakdown.lateFee = round2(lateFee);

        int totalLocalSeconds = 0;
        int totalStdSeconds = 0;
        int totalIsdSeconds = 0;

        for (CallRecord call : calls) {
            int secs = call.getDurationSeconds();
            breakdown.totalCallSeconds += secs;

            if (call.getCallType() == CallType.LOCAL) {
                totalLocalSeconds += secs;
            } else if (call.getCallType() == CallType.STD) {
                totalStdSeconds += secs;
            } else if (call.getCallType() == CallType.ISD) {
                totalIsdSeconds += secs;
            }
        }

        // Minutes are rounded up per telecom standard (pulse rate: 60s)
        int localMinutes = (int) Math.ceil(totalLocalSeconds / 60.0);
        int stdMinutes = (int) Math.ceil(totalStdSeconds / 60.0);
        int isdMinutes = (int) Math.ceil(totalIsdSeconds / 60.0);

        breakdown.totalCallMinutes = localMinutes + stdMinutes + isdMinutes;

        // Apply Free Minutes allowance against Local calls first
        int availableFreeMins = plan.getFreeMinutes();
        int chargeableLocalMins = localMinutes;

        if (availableFreeMins > 0) {
            if (chargeableLocalMins <= availableFreeMins) {
                breakdown.freeMinutesUsed = chargeableLocalMins;
                breakdown.freeMinutesRemaining = availableFreeMins - chargeableLocalMins;
                chargeableLocalMins = 0;
            } else {
                breakdown.freeMinutesUsed = availableFreeMins;
                breakdown.freeMinutesRemaining = 0;
                chargeableLocalMins -= availableFreeMins;
            }
        }

        breakdown.localCharges = round2(chargeableLocalMins * plan.getRatePerMinLocal());
        breakdown.stdCharges = round2(stdMinutes * plan.getRatePerMinStd());
        breakdown.isdCharges = round2(isdMinutes * plan.getRatePerMinIsd());

        breakdown.totalCallCharges = round2(breakdown.localCharges + breakdown.stdCharges + breakdown.isdCharges);

        // Subtotal = Call charges + Monthly rental
        double subtotal = breakdown.totalCallCharges + breakdown.rentalCharges;

        // Tax Calculation (18% GST)
        breakdown.taxAmount = round2(subtotal * (DEFAULT_TAX_PERCENTAGE / 100.0));

        // Final Payable Amount = Subtotal + Tax + LateFee - Discount
        double rawTotal = subtotal + breakdown.taxAmount + breakdown.lateFee - breakdown.discountAmount;
        breakdown.totalPayable = round2(Math.max(0.0, rawTotal));

        return breakdown;
    }

    /**
     * Assembles a persistent Bill entity from calculation results.
     */
    public Bill generateBill(Customer customer, TariffPlan plan, List<CallRecord> calls,
                             int month, int year, double discount, double lateFee) {
        BillingBreakdown breakdown = computeBillBreakdown(plan, calls, discount, lateFee);

        String billNumber = String.format("INV-%04d%02d-%03d", year, month, customer.getId());
        LocalDate generatedDate = LocalDate.now();
        LocalDate dueDate = generatedDate.plusDays(15); // 15 days payment window

        Bill bill = new Bill();
        bill.setBillNumber(billNumber);
        bill.setCustomerId(customer.getId());
        bill.setCustomerName(customer.getName());
        bill.setCustomerPhone(customer.getPhoneNumber());
        bill.setCustomerAddress(customer.getAddress());
        bill.setPlanName(plan.getName());
        bill.setBillingMonth(month);
        bill.setBillingYear(year);
        bill.setTotalCallCharges(breakdown.totalCallCharges);
        bill.setRentalCharges(breakdown.rentalCharges);
        bill.setTaxPercentage(DEFAULT_TAX_PERCENTAGE);
        bill.setTaxAmount(breakdown.taxAmount);
        bill.setDiscountAmount(breakdown.discountAmount);
        bill.setLateFee(breakdown.lateFee);
        bill.setTotalAmount(breakdown.totalPayable);
        bill.setDueDate(dueDate.toString());
        bill.setPaymentStatus("UNPAID");
        bill.setGeneratedDate(generatedDate.toString());

        return bill;
    }

    /**
     * Computes the individual call cost for a specific CallRecord based on its duration and plan.
     */
    public double computeCallCost(CallType type, int durationSeconds, TariffPlan plan) {
        int minutes = (int) Math.ceil(durationSeconds / 60.0);
        double rate = plan.getRateForCallType(type);
        return round2(minutes * rate);
    }

    public static double round2(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
