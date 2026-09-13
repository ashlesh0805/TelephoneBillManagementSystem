package com.telecom.controller;

import com.telecom.model.Bill;
import com.telecom.model.CallRecord;
import com.telecom.service.PdfInvoiceService;
import com.telecom.util.AlertUtil;
import com.telecom.util.CurrencyUtil;
import com.telecom.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

/**
 * Controller for visual authentic telecom invoice preview (Airtel/Jio/BSNL style).
 */
public class InvoicePreviewController {
    @FXML private VBox printableInvoiceArea;
    @FXML private Label invoiceNumberLabel;
    @FXML private Label billDateLabel;
    @FXML private Label dueDateLabel;
    @FXML private Label statusStampLabel;

    @FXML private Label customerNameLabel;
    @FXML private Label customerPhoneLabel;
    @FXML private Label customerAddressLabel;
    @FXML private Label customerIdLabel;

    @FXML private Label planNameLabel;
    @FXML private Label billingPeriodLabel;

    @FXML private Label rentalChargesLabel;
    @FXML private Label callChargesLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label cgstLabel;
    @FXML private Label sgstLabel;
    @FXML private Label lateFeeLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalPayableLabel;

    @FXML private TableView<CallRecord> itemizedCallsTable;
    @FXML private TableColumn<CallRecord, String> colCallDate;
    @FXML private TableColumn<CallRecord, String> colCallDest;
    @FXML private TableColumn<CallRecord, String> colCallCategory;
    @FXML private TableColumn<CallRecord, String> colCallDuration;
    @FXML private TableColumn<CallRecord, String> colCallCost;

    private Bill currentBill;
    private List<CallRecord> currentCalls;
    private final PdfInvoiceService pdfService = new PdfInvoiceService();

    public void initData(Bill bill, List<CallRecord> calls) {
        this.currentBill = bill;
        this.currentCalls = calls;

        // Header
        invoiceNumberLabel.setText("INVOICE #" + bill.getBillNumber());
        billDateLabel.setText("Bill Date: " + DateUtil.formatDisplayDate(bill.getGeneratedDate()));
        dueDateLabel.setText("Due Date: " + DateUtil.formatDisplayDate(bill.getDueDate()));

        // Status Stamp
        statusStampLabel.setText(bill.getPaymentStatus().toUpperCase());
        if ("PAID".equalsIgnoreCase(bill.getPaymentStatus())) {
            statusStampLabel.setStyle("-fx-border-color: #16a34a; -fx-text-fill: #16a34a; -fx-border-width: 2px; -fx-padding: 4px 12px; -fx-border-radius: 6px; -fx-font-weight: 900; -fx-font-size: 14px;");
        } else if ("OVERDUE".equalsIgnoreCase(bill.getPaymentStatus())) {
            statusStampLabel.setStyle("-fx-border-color: #dc2626; -fx-text-fill: #dc2626; -fx-border-width: 2px; -fx-padding: 4px 12px; -fx-border-radius: 6px; -fx-font-weight: 900; -fx-font-size: 14px;");
        } else {
            statusStampLabel.setStyle("-fx-border-color: #d97706; -fx-text-fill: #d97706; -fx-border-width: 2px; -fx-padding: 4px 12px; -fx-border-radius: 6px; -fx-font-weight: 900; -fx-font-size: 14px;");
        }

        // Customer Info
        customerNameLabel.setText(bill.getCustomerName());
        customerPhoneLabel.setText("Mobile: " + bill.getCustomerPhone());
        customerAddressLabel.setText(bill.getCustomerAddress() != null ? bill.getCustomerAddress() : "Standard Circle");
        customerIdLabel.setText("Account ID: TELCO-" + String.format("%05d", bill.getCustomerId()));

        // Plan Info
        planNameLabel.setText(bill.getPlanName() != null ? bill.getPlanName() : "Standard Tariff");
        billingPeriodLabel.setText("Billing Cycle: " + bill.getBillingPeriod());

        // Breakdown Summary
        rentalChargesLabel.setText(CurrencyUtil.format(bill.getRentalCharges()));
        callChargesLabel.setText(CurrencyUtil.format(bill.getTotalCallCharges()));
        subtotalLabel.setText(CurrencyUtil.format(bill.getSubtotal()));

        double cgst = bill.getTaxAmount() / 2.0;
        double sgst = bill.getTaxAmount() / 2.0;
        cgstLabel.setText(CurrencyUtil.format(cgst));
        sgstLabel.setText(CurrencyUtil.format(sgst));

        lateFeeLabel.setText(CurrencyUtil.format(bill.getLateFee()));
        discountLabel.setText("-" + CurrencyUtil.format(bill.getDiscountAmount()));
        totalPayableLabel.setText(CurrencyUtil.format(bill.getTotalAmount()));

        // Itemized Table
        setupItemizedTable();
    }

    private void setupItemizedTable() {
        colCallDate.setCellValueFactory(c -> new SimpleStringProperty(DateUtil.formatDisplayDateTime(c.getValue().getCallTimestamp())));
        colCallDest.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDestinationNumber()));
        colCallCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCallType().getDisplayName()));
        colCallDuration.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFormattedDuration()));
        colCallCost.setCellValueFactory(c -> new SimpleStringProperty(CurrencyUtil.format(c.getValue().getComputedCost())));

        itemizedCallsTable.setItems(FXCollections.observableArrayList(currentCalls));
    }

    @FXML
    private void handleDownloadPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Invoice PDF");
        fileChooser.setInitialFileName(currentBill.getBillNumber() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Documents (*.pdf)", "*.pdf"));

        File file = fileChooser.showSaveDialog(printableInvoiceArea.getScene().getWindow());
        if (file != null) {
            try {
                pdfService.generateInvoicePdf(currentBill, currentCalls, file);
                AlertUtil.showInfo(printableInvoiceArea.getScene().getWindow(), "PDF Exported", "Invoice saved successfully to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                AlertUtil.showError(printableInvoiceArea.getScene().getWindow(), "PDF Error", "Failed to export PDF: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handlePrintInvoice() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(printableInvoiceArea.getScene().getWindow())) {
            boolean success = job.printPage(printableInvoiceArea);
            if (success) {
                job.endJob();
                AlertUtil.showInfo(printableInvoiceArea.getScene().getWindow(), "Print", "Print job sent successfully.");
            }
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) printableInvoiceArea.getScene().getWindow();
        stage.close();
    }
}
