package com.telecom.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.telecom.model.Bill;
import com.telecom.model.CallRecord;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Service to generate high-quality, professional telecom invoices in PDF format using OpenPDF.
 * Mimics real-world telecom provider bills (such as Airtel / Jio / BSNL).
 */
public class PdfInvoiceService {
    private static final DecimalFormat CURRENCY_FMT = new DecimalFormat("₹#,##0.00");

    // Corporate Color Palette
    private static final Color PRIMARY_NAVY = new Color(15, 23, 42);     // #0f172a
    private static final Color ACCENT_BLUE = new Color(37, 99, 235);     // #2563eb
    private static final Color BG_LIGHT = new Color(248, 250, 252);       // #f8fafc
    private static final Color BORDER_GRAY = new Color(226, 232, 240);    // #e2e8f0
    private static final Color TEXT_DARK = new Color(30, 41, 59);        // #1e293b
    private static final Color TEXT_MUTED = new Color(100, 116, 139);    // #64748b
    private static final Color SUCCESS_GREEN = new Color(22, 163, 74);   // #16a34a
    private static final Color DANGER_RED = new Color(220, 38, 38);      // #dc2626
    private static final Color WARNING_AMBER = new Color(217, 119, 6);   // #d97706

    // Typography
    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, PRIMARY_NAVY);
    private static final Font FONT_SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA, 8, TEXT_MUTED);
    private static final Font FONT_SECTION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, ACCENT_BLUE);
    private static final Font FONT_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, TEXT_DARK);
    private static final Font FONT_REGULAR = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, TEXT_DARK);
    private static final Font FONT_MUTED = FontFactory.getFont(FontFactory.HELVETICA, 8, TEXT_MUTED);
    private static final Font FONT_WHITE_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font FONT_TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, PRIMARY_NAVY);

    /**
     * Generates a complete PDF invoice file for a Bill and its corresponding Call Records.
     */
    public void generateInvoicePdf(Bill bill, List<CallRecord> calls, File destinationFile) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(destinationFile)) {
            exportToStream(bill, calls, fos);
        }
    }

    /**
     * Writes the PDF document to any generic OutputStream.
     */
    public void exportToStream(Bill bill, List<CallRecord> calls, OutputStream out) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, out);
        document.open();

        // 1. Header Bar: Brand Logo & Invoice Metadata
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{60f, 40f});
        headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        // Company Details (Left)
        PdfPCell compCell = new PdfPCell();
        compCell.setBorder(Rectangle.NO_BORDER);
        Paragraph compName = new Paragraph("APEX TELECOM", FONT_TITLE);
        Paragraph compSlogan = new Paragraph("Next-Gen Global Telecommunications & Broadband", FONT_SUBTITLE);
        Paragraph compTax = new Paragraph("GSTIN: 27AAACA1234F1Z5 | Telco Lic: IND-98234-DOT\nSupport: 1800-11-2233 | contact@apextelecom.com", FONT_MUTED);
        compCell.addElement(compName);
        compCell.addElement(compSlogan);
        compCell.addElement(compTax);
        headerTable.addCell(compCell);

        // Invoice Badge (Right)
        PdfPCell invMetaCell = new PdfPCell();
        invMetaCell.setBorder(Rectangle.NO_BORDER);
        invMetaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph invTitle = new Paragraph("TAX INVOICE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, ACCENT_BLUE));
        invTitle.setAlignment(Element.ALIGN_RIGHT);
        Paragraph invNum = new Paragraph("Invoice #: " + bill.getBillNumber(), FONT_BOLD);
        invNum.setAlignment(Element.ALIGN_RIGHT);
        Paragraph invDate = new Paragraph("Bill Date: " + bill.getGeneratedDate(), FONT_REGULAR);
        invDate.setAlignment(Element.ALIGN_RIGHT);
        Paragraph dueDate = new Paragraph("Payment Due Date: " + bill.getDueDate(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, DANGER_RED));
        dueDate.setAlignment(Element.ALIGN_RIGHT);

        // Status Badge
        Color statusColor = switch (bill.getPaymentStatus().toUpperCase()) {
            case "PAID" -> SUCCESS_GREEN;
            case "OVERDUE" -> DANGER_RED;
            default -> WARNING_AMBER;
        };
        Paragraph statusBadge = new Paragraph("STATUS: " + bill.getPaymentStatus().toUpperCase(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, statusColor));
        statusBadge.setAlignment(Element.ALIGN_RIGHT);

        invMetaCell.addElement(invTitle);
        invMetaCell.addElement(invNum);
        invMetaCell.addElement(invDate);
        invMetaCell.addElement(dueDate);
        invMetaCell.addElement(statusBadge);
        headerTable.addCell(invMetaCell);

        document.add(headerTable);
        document.add(new Paragraph(" ")); // Spacer

        // 2. Customer & Plan Details (2 Cards)
        PdfPTable infoCardTable = new PdfPTable(2);
        infoCardTable.setWidthPercentage(100);
        infoCardTable.setWidths(new float[]{50f, 50f});
        infoCardTable.setSpacingAfter(10f);

        // Customer Info Card
        PdfPCell custCard = new PdfPCell();
        custCard.setBackgroundColor(BG_LIGHT);
        custCard.setBorderColor(BORDER_GRAY);
        custCard.setPadding(10f);
        custCard.addElement(new Paragraph("BILLED TO", FONT_SECTION));
        custCard.addElement(new Paragraph(bill.getCustomerName(), FONT_BOLD));
        custCard.addElement(new Paragraph("Phone: " + bill.getCustomerPhone(), FONT_REGULAR));
        custCard.addElement(new Paragraph("Address: " + (bill.getCustomerAddress() != null ? bill.getCustomerAddress() : "N/A"), FONT_REGULAR));
        infoCardTable.addCell(custCard);

        // Plan & Period Card
        PdfPCell planCard = new PdfPCell();
        planCard.setBackgroundColor(BG_LIGHT);
        planCard.setBorderColor(BORDER_GRAY);
        planCard.setPadding(10f);
        planCard.addElement(new Paragraph("SUBSCRIPTION & BILLING PERIOD", FONT_SECTION));
        planCard.addElement(new Paragraph("Tariff Plan: " + (bill.getPlanName() != null ? bill.getPlanName() : "Standard"), FONT_BOLD));
        planCard.addElement(new Paragraph("Billing Period: " + bill.getBillingPeriod(), FONT_REGULAR));
        planCard.addElement(new Paragraph("Customer ID: CUST-" + String.format("%05d", bill.getCustomerId()), FONT_REGULAR));
        infoCardTable.addCell(planCard);

        document.add(infoCardTable);

        // 3. Billing Summary Table
        Paragraph chargesTitle = new Paragraph("SUMMARY OF CHARGES", FONT_SECTION);
        chargesTitle.setSpacingAfter(6f);
        document.add(chargesTitle);

        PdfPTable summaryTable = new PdfPTable(3);
        summaryTable.setWidthPercentage(100);
        summaryTable.setWidths(new float[]{60f, 20f, 20f});
        summaryTable.setSpacingAfter(15f);

        // Header
        addTableHeaderCell(summaryTable, "Description");
        addTableHeaderCell(summaryTable, "Rate / Type");
        addTableHeaderCell(summaryTable, "Amount (INR)");

        // Rows
        addTableRow(summaryTable, "Monthly Tariff Plan Rental", "Fixed", CURRENCY_FMT.format(bill.getRentalCharges()));
        addTableRow(summaryTable, "Telephone Usage & Call Charges", "Itemized", CURRENCY_FMT.format(bill.getTotalCallCharges()));
        
        double subtotal = bill.getSubtotal();
        addTableRow(summaryTable, "Subtotal (Taxable Value)", "", CURRENCY_FMT.format(subtotal));

        double cgst = bill.getTaxAmount() / 2.0;
        double sgst = bill.getTaxAmount() / 2.0;
        addTableRow(summaryTable, "Central GST (CGST @ 9%)", "9.0%", CURRENCY_FMT.format(cgst));
        addTableRow(summaryTable, "State GST (SGST @ 9%)", "9.0%", CURRENCY_FMT.format(sgst));

        if (bill.getLateFee() > 0) {
            addTableRow(summaryTable, "Late Payment Surcharge / Fee", "Overdue Fee", CURRENCY_FMT.format(bill.getLateFee()));
        }
        if (bill.getDiscountAmount() > 0) {
            addTableRow(summaryTable, "Promotional Rebate / Discount", "Credit", "-" + CURRENCY_FMT.format(bill.getDiscountAmount()));
        }

        // Total Amount Row
        PdfPCell totalDescCell = new PdfPCell(new Phrase("TOTAL AMOUNT PAYABLE", FONT_TOTAL));
        totalDescCell.setColspan(2);
        totalDescCell.setBackgroundColor(new Color(238, 242, 255)); // Indigo-50
        totalDescCell.setBorderColor(BORDER_GRAY);
        totalDescCell.setPadding(8f);
        summaryTable.addCell(totalDescCell);

        PdfPCell totalValCell = new PdfPCell(new Phrase(CURRENCY_FMT.format(bill.getTotalAmount()),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, ACCENT_BLUE)));
        totalValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalValCell.setBackgroundColor(new Color(238, 242, 255));
        totalValCell.setBorderColor(BORDER_GRAY);
        totalValCell.setPadding(8f);
        summaryTable.addCell(totalValCell);

        document.add(summaryTable);

        // 4. Itemized Call Detail Records (CDR)
        Paragraph cdrTitle = new Paragraph("ITEMIZED CALL DETAIL RECORDS (CDR)", FONT_SECTION);
        cdrTitle.setSpacingAfter(6f);
        document.add(cdrTitle);

        PdfPTable cdrTable = new PdfPTable(5);
        cdrTable.setWidthPercentage(100);
        cdrTable.setWidths(new float[]{25f, 25f, 20f, 15f, 15f});
        cdrTable.setSpacingAfter(15f);

        addTableHeaderCell(cdrTable, "Date & Time");
        addTableHeaderCell(cdrTable, "Destination Number");
        addTableHeaderCell(cdrTable, "Call Category");
        addTableHeaderCell(cdrTable, "Duration");
        addTableHeaderCell(cdrTable, "Cost (INR)");

        if (calls == null || calls.isEmpty()) {
            PdfPCell emptyCell = new PdfPCell(new Phrase("No telephone calls logged during this billing cycle.", FONT_MUTED));
            emptyCell.setColspan(5);
            emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            emptyCell.setPadding(10f);
            emptyCell.setBorderColor(BORDER_GRAY);
            cdrTable.addCell(emptyCell);
        } else {
            boolean alternate = false;
            for (CallRecord call : calls) {
                Color rowBg = alternate ? BG_LIGHT : Color.WHITE;
                addCdrRow(cdrTable, call.getCallTimestamp(), rowBg, Element.ALIGN_LEFT);
                addCdrRow(cdrTable, call.getDestinationNumber(), rowBg, Element.ALIGN_LEFT);
                addCdrRow(cdrTable, call.getCallType().getDisplayName(), rowBg, Element.ALIGN_LEFT);
                addCdrRow(cdrTable, call.getFormattedDuration(), rowBg, Element.ALIGN_CENTER);
                addCdrRow(cdrTable, CURRENCY_FMT.format(call.getComputedCost()), rowBg, Element.ALIGN_RIGHT);
                alternate = !alternate;
            }
        }
        document.add(cdrTable);

        // 5. Payment Terms & Remittance Footer
        PdfPTable footerTable = new PdfPTable(1);
        footerTable.setWidthPercentage(100);
        PdfPCell footerCell = new PdfPCell();
        footerCell.setBackgroundColor(BG_LIGHT);
        footerCell.setBorderColor(BORDER_GRAY);
        footerCell.setPadding(8f);

        Paragraph footerHeading = new Paragraph("PAYMENT INSTRUCTIONS & REMITTANCE:", FONT_BOLD);
        Paragraph footerText = new Paragraph(
                "• Pay via UPI: apextelecom@upi (Scan & Pay) or Net Banking / Credit Card via the TelcoPay Portal.\n" +
                "• Overdue accounts will incur a late fee of ₹50.00 after the due date.\n" +
                "• This is an electronically generated statement and does not require a physical signature.\n" +
                "• For billing discrepancies or customer assistance, please reach out to care@apextelecom.com.",
                FONT_MUTED
        );
        footerCell.addElement(footerHeading);
        footerCell.addElement(footerText);
        footerTable.addCell(footerCell);

        document.add(footerTable);

        document.close();
    }

    private static void addTableHeaderCell(PdfPTable table, String title) {
        PdfPCell cell = new PdfPCell(new Phrase(title, FONT_WHITE_BOLD));
        cell.setBackgroundColor(PRIMARY_NAVY);
        cell.setPadding(6f);
        cell.setBorderColor(PRIMARY_NAVY);
        table.addCell(cell);
    }

    private static void addTableRow(PdfPTable table, String desc, String type, String amount) {
        PdfPCell c1 = new PdfPCell(new Phrase(desc, FONT_REGULAR));
        c1.setPadding(5f);
        c1.setBorderColor(BORDER_GRAY);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(type, FONT_MUTED));
        c2.setPadding(5f);
        c2.setBorderColor(BORDER_GRAY);
        table.addCell(c2);

        PdfPCell c3 = new PdfPCell(new Phrase(amount, FONT_BOLD));
        c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c3.setPadding(5f);
        c3.setBorderColor(BORDER_GRAY);
        table.addCell(c3);
    }

    private static void addCdrRow(PdfPTable table, String text, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_REGULAR));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(align);
        cell.setPadding(5f);
        cell.setBorderColor(BORDER_GRAY);
        table.addCell(cell);
    }
}
