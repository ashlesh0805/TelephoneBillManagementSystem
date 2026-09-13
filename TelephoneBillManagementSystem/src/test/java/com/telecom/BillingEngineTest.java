package com.telecom;

import com.telecom.model.CallRecord;
import com.telecom.model.CallType;
import com.telecom.model.TariffPlan;
import com.telecom.service.BillingEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite specifically testing BillingEngine calculations,
 * free-minute deductions, tax computations, and edge cases.
 */
public class BillingEngineTest {

    private BillingEngine billingEngine;
    private TariffPlan testPlan;

    @BeforeEach
    public void setUp() {
        billingEngine = new BillingEngine();
        // Test plan: 100 free mins, ₹0.50/min Local, ₹1.00/min STD, ₹5.00/min ISD, ₹200.00 Monthly Rental
        testPlan = new TariffPlan(
                1, "Test Plan", 0.50, 1.00, 5.00,
                200.00, 100, 0.25, 10.00, "Test tariff"
        );
    }

    @Test
    @DisplayName("Should apply free minutes when local call duration is under allowance")
    public void testUnderFreeMinutesAllowance() {
        List<CallRecord> calls = new ArrayList<>();
        // 2 calls totaling 80 minutes (4800 seconds)
        calls.add(createCall(CallType.LOCAL, 3000)); // 50 mins
        calls.add(createCall(CallType.LOCAL, 1800)); // 30 mins

        BillingEngine.BillingBreakdown bd = billingEngine.computeBillBreakdown(testPlan, calls, 0.0, 0.0);

        assertEquals(80, bd.totalCallMinutes);
        assertEquals(80, bd.freeMinutesUsed);
        assertEquals(20, bd.freeMinutesRemaining);
        assertEquals(0.0, bd.localCharges, 0.001, "Local call charges should be 0 because 80 mins <= 100 free mins");
        assertEquals(0.0, bd.totalCallCharges, 0.001);
        assertEquals(200.00, bd.rentalCharges, 0.001);
        assertEquals(36.00, bd.taxAmount, 0.001, "GST 18% of 200 should be 36.00");
        assertEquals(236.00, bd.totalPayable, 0.001);
    }

    @Test
    @DisplayName("Should charge only excess minutes when local call duration exceeds allowance")
    public void testExceedingFreeMinutesAllowance() {
        List<CallRecord> calls = new ArrayList<>();
        // Total 120 minutes (7200 seconds) of local calls
        calls.add(createCall(CallType.LOCAL, 7200));

        BillingEngine.BillingBreakdown bd = billingEngine.computeBillBreakdown(testPlan, calls, 0.0, 0.0);

        assertEquals(120, bd.totalCallMinutes);
        assertEquals(100, bd.freeMinutesUsed);
        assertEquals(0, bd.freeMinutesRemaining);
        // Excess 20 mins * 0.50 = 10.00
        assertEquals(10.00, bd.localCharges, 0.001);
        assertEquals(10.00, bd.totalCallCharges, 0.001);

        double subtotal = 10.00 + 200.00; // 210.00
        double expectedTax = 210.00 * 0.18; // 37.80
        assertEquals(37.80, bd.taxAmount, 0.001);
        assertEquals(247.80, bd.totalPayable, 0.001);
    }

    @Test
    @DisplayName("Should correctly calculate mixed Local, STD, and ISD charges")
    public void testMixedCallCategories() {
        List<CallRecord> calls = new ArrayList<>();
        // 100 mins Local (all covered by 100 free mins)
        calls.add(createCall(CallType.LOCAL, 6000));
        // 10 mins STD: 10 * 1.00 = ₹10.00
        calls.add(createCall(CallType.STD, 600));
        // 5 mins ISD: 5 * 5.00 = ₹25.00
        calls.add(createCall(CallType.ISD, 300));

        BillingEngine.BillingBreakdown bd = billingEngine.computeBillBreakdown(testPlan, calls, 0.0, 0.0);

        assertEquals(0.0, bd.localCharges, 0.001);
        assertEquals(10.00, bd.stdCharges, 0.001);
        assertEquals(25.00, bd.isdCharges, 0.001);
        assertEquals(35.00, bd.totalCallCharges, 0.001);

        double subtotal = 35.00 + 200.00; // 235.00
        double expectedTax = 235.00 * 0.18; // 42.30
        assertEquals(42.30, bd.taxAmount, 0.001);
        assertEquals(277.30, bd.totalPayable, 0.001);
    }

    @Test
    @DisplayName("Should correctly apply discount and late surcharge")
    public void testDiscountAndLateFee() {
        List<CallRecord> calls = new ArrayList<>();
        double discount = 20.00;
        double lateFee = 50.00;

        BillingEngine.BillingBreakdown bd = billingEngine.computeBillBreakdown(testPlan, calls, discount, lateFee);

        // Subtotal = 200 (rental)
        // Tax = 36.00
        // Total = 200 + 36 + 50 - 20 = 266.00
        assertEquals(266.00, bd.totalPayable, 0.001);
    }

    @Test
    @DisplayName("Should handle zero-call billing period with only rental and taxes")
    public void testZeroCalls() {
        List<CallRecord> calls = new ArrayList<>();
        BillingEngine.BillingBreakdown bd = billingEngine.computeBillBreakdown(testPlan, calls, 0.0, 0.0);

        assertEquals(0, bd.totalCallMinutes);
        assertEquals(0.0, bd.totalCallCharges, 0.001);
        assertEquals(200.00, bd.rentalCharges, 0.001);
        assertEquals(36.00, bd.taxAmount, 0.001);
        assertEquals(236.00, bd.totalPayable, 0.001);
    }

    private CallRecord createCall(CallType type, int seconds) {
        CallRecord c = new CallRecord();
        c.setCallType(type);
        c.setDurationSeconds(seconds);
        return c;
    }
}
