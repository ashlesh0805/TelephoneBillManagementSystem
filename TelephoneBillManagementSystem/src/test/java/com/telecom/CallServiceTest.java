package com.telecom;

import com.telecom.model.CallType;
import com.telecom.service.CallService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CallService telephone prefix classification and format validation.
 */
public class CallServiceTest {

    @Test
    @DisplayName("Should detect International ISD calls")
    public void testIsdDetection() {
        assertEquals(CallType.ISD, CallService.detectCallType("+14155552671"));
        assertEquals(CallType.ISD, CallService.detectCallType("+442079460912"));
        assertEquals(CallType.ISD, CallService.detectCallType("+81312345678"));
        assertEquals(CallType.ISD, CallService.detectCallType("0014155552671"));
    }

    @Test
    @DisplayName("Should detect National STD calls")
    public void testStdDetection() {
        assertEquals(CallType.STD, CallService.detectCallType("02224567890")); // Mumbai STD
        assertEquals(CallType.STD, CallService.detectCallType("08023456789")); // Bengaluru STD
        assertEquals(CallType.STD, CallService.detectCallType("01123456789")); // Delhi STD
    }

    @Test
    @DisplayName("Should detect Local calls")
    public void testLocalDetection() {
        assertEquals(CallType.LOCAL, CallService.detectCallType("+919820011223"));
        assertEquals(CallType.LOCAL, CallService.detectCallType("9820011223"));
        assertEquals(CallType.LOCAL, CallService.detectCallType("+91 98450 33445"));
    }

    @Test
    @DisplayName("Should validate phone numbers correctly")
    public void testPhoneValidation() {
        assertTrue(CallService.isValidPhoneNumber("+919820011223"));
        assertTrue(CallService.isValidPhoneNumber("9820011223"));
        assertTrue(CallService.isValidPhoneNumber("02224567890"));
        assertTrue(CallService.isValidPhoneNumber("+14155552671"));

        assertFalse(CallService.isValidPhoneNumber("12345")); // too short
        assertFalse(CallService.isValidPhoneNumber("phone123")); // non numeric
        assertFalse(CallService.isValidPhoneNumber("")); // empty
        assertFalse(CallService.isValidPhoneNumber(null));
    }
}
