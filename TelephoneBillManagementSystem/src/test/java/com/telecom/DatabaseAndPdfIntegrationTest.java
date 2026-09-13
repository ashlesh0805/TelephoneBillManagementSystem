package com.telecom;

import com.telecom.dao.BillDAO;
import com.telecom.dao.CallRecordDAO;
import com.telecom.dao.CustomerDAO;
import com.telecom.dao.DatabaseManager;
import com.telecom.model.Bill;
import com.telecom.model.CallRecord;
import com.telecom.model.Customer;
import com.telecom.service.PdfInvoiceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test verifying Database initialization, seed data population,
 * and OpenPDF invoice generation.
 */
public class DatabaseAndPdfIntegrationTest {

    @Test
    @DisplayName("Verify Database schema initialization, seed data, and PDF generation")
    public void testDatabaseAndPdfGeneration() throws Exception {
        // 1. Initialize SQLite Database
        DatabaseManager db = DatabaseManager.getInstance();
        assertNotNull(db);

        // 2. Verify Seed Customers
        CustomerDAO customerDAO = new CustomerDAO();
        List<Customer> customers = customerDAO.getAllCustomers();
        assertFalse(customers.isEmpty(), "Seed data customers should be populated");
        assertTrue(customers.size() >= 6, "Expected at least 6 sample customers");

        // 3. Verify Seed Bills
        BillDAO billDAO = new BillDAO();
        List<Bill> bills = billDAO.getAllBills();
        assertFalse(bills.isEmpty(), "Seed data bills should be populated");

        // 4. Test OpenPDF Invoice Generation
        Bill firstBill = bills.get(0);
        CallRecordDAO callDAO = new CallRecordDAO();
        List<CallRecord> calls = callDAO.getCallsByCustomerAndPeriod(
                firstBill.getCustomerId(), firstBill.getBillingMonth(), firstBill.getBillingYear()
        );

        PdfInvoiceService pdfService = new PdfInvoiceService();
        File tempPdf = File.createTempFile("test_invoice_", ".pdf");
        tempPdf.deleteOnExit();

        pdfService.generateInvoicePdf(firstBill, calls, tempPdf);

        assertTrue(tempPdf.exists(), "PDF file must exist");
        assertTrue(tempPdf.length() > 2000, "PDF file must be non-empty (larger than 2KB)");
    }
}
