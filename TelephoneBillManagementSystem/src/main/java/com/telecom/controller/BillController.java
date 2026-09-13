package com.telecom.controller;

import com.telecom.dao.BillDAO;
import com.telecom.dao.CallRecordDAO;
import com.telecom.dao.CustomerDAO;
import com.telecom.dao.PaymentDAO;
import com.telecom.dao.TariffPlanDAO;
import com.telecom.model.Bill;
import com.telecom.model.CallRecord;
import com.telecom.model.Customer;
import com.telecom.model.Payment;
import com.telecom.model.TariffPlan;
import com.telecom.service.BillingEngine;
import com.telecom.service.CsvService;
import com.telecom.service.PdfInvoiceService;
import com.telecom.util.AlertUtil;
import com.telecom.util.CurrencyUtil;
import com.telecom.util.DateUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for generating bills, tracking invoice payments, exporting OpenPDF invoices,
 * and previewing telecom statements.
 */
public class BillController {
    private static final Logger LOGGER = Logger.getLogger(BillController.class.getName());

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TableView<Bill> billTable;
    @FXML private TableColumn<Bill, String> colBillNo;
    @FXML private TableColumn<Bill, String> colCustomer;
    @FXML private TableColumn<Bill, String> colPeriod;
    @FXML private TableColumn<Bill, String> colRental;
    @FXML private TableColumn<Bill, String> colCalls;
    @FXML private TableColumn<Bill, String> colTax;
    @FXML private TableColumn<Bill, String> colTotal;
    @FXML private TableColumn<Bill, String> colDueDate;
    @FXML private TableColumn<Bill, String> colStatus;
    @FXML private TableColumn<Bill, Void> colActions;

    private final BillDAO billDAO = new BillDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final TariffPlanDAO planDAO = new TariffPlanDAO();
    private final CallRecordDAO callDAO = new CallRecordDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final BillingEngine billingEngine = new BillingEngine();
    private final PdfInvoiceService pdfService = new PdfInvoiceService();
    private final CsvService csvService = new CsvService();

    private final ObservableList<Bill> allBillsList = FXCollections.observableArrayList();
    private final ObservableList<Bill> filteredBillsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadBills();
    }

    private void setupTableColumns() {
        colBillNo.setCellValueFactory(new PropertyValueFactory<>("billNumber"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colPeriod.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBillingPeriod()));

        colRental.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(CurrencyUtil.format(cellData.getValue().getRentalCharges())));
        colCalls.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(CurrencyUtil.format(cellData.getValue().getTotalCallCharges())));
        colTax.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(CurrencyUtil.format(cellData.getValue().getTaxAmount())));
        colTotal.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(CurrencyUtil.format(cellData.getValue().getTotalAmount())));

        colDueDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(DateUtil.formatDisplayDate(cellData.getValue().getDueDate())));

        // Status Badge Cell
        colStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(status);
                    badge.getStyleClass().add("badge");
                    switch (status.toUpperCase()) {
                        case "PAID" -> badge.getStyleClass().add("badge-paid");
                        case "OVERDUE" -> badge.getStyleClass().add("badge-overdue");
                        default -> badge.getStyleClass().add("badge-unpaid");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        // Row Actions: View Invoice, Export PDF, Pay Bill
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button pdfBtn = new Button("PDF");
            private final Button payBtn = new Button("Pay");
            private final HBox pane = new HBox(5, viewBtn, pdfBtn, payBtn);

            {
                viewBtn.getStyleClass().addAll("btn-secondary", "btn-sm");
                pdfBtn.getStyleClass().addAll("btn-primary", "btn-sm");
                payBtn.getStyleClass().addAll("btn-success", "btn-sm");

                viewBtn.setOnAction(e -> {
                    Bill b = getTableView().getItems().get(getIndex());
                    openInvoicePreview(b);
                });

                pdfBtn.setOnAction(e -> {
                    Bill b = getTableView().getItems().get(getIndex());
                    exportBillPdf(b);
                });

                payBtn.setOnAction(e -> {
                    Bill b = getTableView().getItems().get(getIndex());
                    openPaymentModal(b);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Bill b = getTableView().getItems().get(getIndex());
                    payBtn.setVisible(!"PAID".equalsIgnoreCase(b.getPaymentStatus()));
                    payBtn.setManaged(!"PAID".equalsIgnoreCase(b.getPaymentStatus()));
                    setGraphic(pane);
                }
            }
        });

        billTable.setItems(filteredBillsList);
    }

    private void setupFilters() {
        statusFilterCombo.setItems(FXCollections.observableArrayList("ALL", "PAID", "UNPAID", "OVERDUE"));
        statusFilterCombo.setValue("ALL");

        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        statusFilterCombo.valueProperty().addListener((obs, o, n) -> applyFilters());
    }

    public void loadBills() {
        try {
            List<Bill> list = billDAO.getAllBills();
            allBillsList.setAll(list);
            applyFilters();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load bills", e);
        }
    }

    private void applyFilters() {
        String query = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String status = statusFilterCombo.getValue();

        List<Bill> filtered = allBillsList.stream().filter(b -> {
            boolean matchQuery = query.isEmpty() ||
                    b.getBillNumber().toLowerCase().contains(query) ||
                    b.getCustomerName().toLowerCase().contains(query) ||
                    b.getCustomerPhone().toLowerCase().contains(query);

            boolean matchStatus = status == null || status.equalsIgnoreCase("ALL") ||
                    b.getPaymentStatus().equalsIgnoreCase(status);

            return matchQuery && matchStatus;
        }).toList();

        filteredBillsList.setAll(filtered);
    }

    @FXML
    private void handleGenerateBill() {
        Dialog<Bill> dialog = new Dialog<>();
        dialog.setTitle("Generate Customer Monthly Bill");
        dialog.setHeaderText("Calculate and assemble a monthly itemized telephone bill");

        ButtonType genBtnType = new ButtonType("Generate & Save Bill", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(genBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);
        grid.setPadding(new Insets(20, 20, 10, 20));

        ComboBox<Customer> customerBox = new ComboBox<>();
        try {
            customerBox.setItems(FXCollections.observableArrayList(customerDAO.getAllCustomers()));
            if (!customerBox.getItems().isEmpty()) {
                customerBox.setValue(customerBox.getItems().get(0));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load customers", e);
        }

        LocalDate now = LocalDate.now();
        ComboBox<Integer> monthBox = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
        monthBox.setValue(now.getMonthValue());

        ComboBox<Integer> yearBox = new ComboBox<>(FXCollections.observableArrayList(now.getYear() - 1, now.getYear(), now.getYear() + 1));
        yearBox.setValue(now.getYear());

        TextField discountField = new TextField("0.0");
        TextField lateFeeField = new TextField("0.0");

        Label previewSummaryLabel = new Label("Select subscriber and cycle to compute summary");
        previewSummaryLabel.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748b;");

        // Dynamic Calculation Preview
        Runnable updateCalcPreview = () -> {
            Customer c = customerBox.getValue();
            Integer m = monthBox.getValue();
            Integer y = yearBox.getValue();
            if (c != null && m != null && y != null) {
                try {
                    TariffPlan plan = planDAO.getPlanById(c.getPlanId());
                    List<CallRecord> calls = callDAO.getCallsByCustomerAndPeriod(c.getId(), m, y);
                    double discount = parseDoubleSafe(discountField.getText());
                    double lateFee = parseDoubleSafe(lateFeeField.getText());

                    BillingEngine.BillingBreakdown bd = billingEngine.computeBillBreakdown(plan, calls, discount, lateFee);
                    previewSummaryLabel.setText(String.format(
                            "Found %d calls (%d mins). Rental: ₹%.2f | Usage: ₹%.2f | GST (18%%): ₹%.2f | Total: ₹%.2f",
                            calls.size(), bd.totalCallMinutes, bd.rentalCharges, bd.totalCallCharges, bd.taxAmount, bd.totalPayable
                    ));
                    previewSummaryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2563eb; -fx-font-weight: bold;");
                } catch (Exception ignored) {}
            }
        };

        customerBox.valueProperty().addListener((o, ov, nv) -> updateCalcPreview.run());
        monthBox.valueProperty().addListener((o, ov, nv) -> updateCalcPreview.run());
        yearBox.valueProperty().addListener((o, ov, nv) -> updateCalcPreview.run());
        discountField.textProperty().addListener((o, ov, nv) -> updateCalcPreview.run());
        lateFeeField.textProperty().addListener((o, ov, nv) -> updateCalcPreview.run());

        updateCalcPreview.run();

        grid.add(new Label("Select Subscriber: *"), 0, 0);
        grid.add(customerBox, 1, 0);
        grid.add(new Label("Billing Month: *"), 0, 1);
        grid.add(monthBox, 1, 1);
        grid.add(new Label("Billing Year: *"), 0, 2);
        grid.add(yearBox, 1, 2);
        grid.add(new Label("Discount (₹):"), 0, 3);
        grid.add(discountField, 1, 3);
        grid.add(new Label("Late Surcharge (₹):"), 0, 4);
        grid.add(lateFeeField, 1, 4);
        grid.add(new Label("Live Calculation:"), 0, 5);
        grid.add(previewSummaryLabel, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == genBtnType) {
                Customer cust = customerBox.getValue();
                int month = monthBox.getValue();
                int year = yearBox.getValue();
                double discount = parseDoubleSafe(discountField.getText());
                double lateFee = parseDoubleSafe(lateFeeField.getText());

                try {
                    // Check if bill already exists for this cycle
                    Bill existing = billDAO.getBillByCustomerAndPeriod(cust.getId(), month, year);
                    if (existing != null) {
                        AlertUtil.showWarning(dialog.getDialogPane().getScene().getWindow(), "Duplicate Bill",
                                "A bill has already been generated for this subscriber for period " + String.format("%02d/%d", month, year) +
                                " (Bill #" + existing.getBillNumber() + ").");
                        return null;
                    }

                    TariffPlan plan = planDAO.getPlanById(cust.getPlanId());
                    List<CallRecord> calls = callDAO.getCallsByCustomerAndPeriod(cust.getId(), month, year);

                    return billingEngine.generateBill(cust, plan, calls, month, year, discount, lateFee);
                } catch (SQLException e) {
                    AlertUtil.showError(dialog.getDialogPane().getScene().getWindow(), "Database Error", e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Bill> result = dialog.showAndWait();
        result.ifPresent(bill -> {
            try {
                billDAO.createBill(bill);
                loadBills();
                AlertUtil.showInfo(billTable.getScene().getWindow(), "Bill Generated Successfully",
                        "Invoice #" + bill.getBillNumber() + " has been generated.\nAmount Payable: " + CurrencyUtil.format(bill.getTotalAmount()));
            } catch (SQLException e) {
                AlertUtil.showError(billTable.getScene().getWindow(), "Generation Failed", "Could not persist bill: " + e.getMessage());
            }
        });
    }

    private void openPaymentModal(Bill bill) {
        Dialog<Payment> dialog = new Dialog<>();
        dialog.setTitle("Record Invoice Payment");
        dialog.setHeaderText("Settle Bill #" + bill.getBillNumber() + " for " + bill.getCustomerName());

        ButtonType payBtnType = new ButtonType("Confirm Payment", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(payBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);
        grid.setPadding(new Insets(20, 20, 10, 20));

        TextField amountField = new TextField(String.format("%.2f", bill.getTotalAmount()));
        ComboBox<String> modeBox = new ComboBox<>(FXCollections.observableArrayList(
                "UPI", "CREDIT_CARD", "DEBIT_CARD", "NET_BANKING", "CASH"
        ));
        modeBox.setValue("UPI");

        TextField refField = new TextField("TXN-" + System.currentTimeMillis());
        TextField notesField = new TextField("Invoice settlement");

        grid.add(new Label("Bill Amount:"), 0, 0);
        grid.add(new Label(CurrencyUtil.format(bill.getTotalAmount()) + " (" + bill.getPaymentStatus() + ")"), 1, 0);
        grid.add(new Label("Amount Paid (₹): *"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Payment Mode: *"), 0, 2);
        grid.add(modeBox, 1, 2);
        grid.add(new Label("Transaction / UTR Ref:"), 0, 3);
        grid.add(refField, 1, 3);
        grid.add(new Label("Notes / Remarks:"), 0, 4);
        grid.add(notesField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == payBtnType) {
                double amount = parseDoubleSafe(amountField.getText());
                if (amount <= 0) {
                    AlertUtil.showError(dialog.getDialogPane().getScene().getWindow(), "Invalid Amount", "Payment amount must be greater than 0.");
                    return null;
                }

                Payment payment = new Payment();
                payment.setBillId(bill.getId());
                payment.setAmountPaid(amount);
                payment.setPaymentDate(LocalDate.now().toString());
                payment.setPaymentMode(modeBox.getValue());
                payment.setTransactionReference(refField.getText().trim());
                payment.setNotes(notesField.getText().trim());
                return payment;
            }
            return null;
        });

        Optional<Payment> result = dialog.showAndWait();
        result.ifPresent(payment -> {
            try {
                paymentDAO.recordPayment(payment);
                double totalPaid = paymentDAO.getTotalPaidForBill(bill.getId());
                String newStatus = totalPaid >= bill.getTotalAmount() ? "PAID" : "PARTIALLY_PAID";
                billDAO.updatePaymentStatus(bill.getId(), newStatus);
                loadBills();
                AlertUtil.showInfo(billTable.getScene().getWindow(), "Payment Recorded",
                        "Payment of " + CurrencyUtil.format(payment.getAmountPaid()) + " successfully recorded.\nInvoice status updated to " + newStatus + ".");
            } catch (SQLException e) {
                AlertUtil.showError(billTable.getScene().getWindow(), "Payment Error", "Failed to record payment: " + e.getMessage());
            }
        });
    }

    private void exportBillPdf(Bill bill) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Invoice PDF");
        fileChooser.setInitialFileName(bill.getBillNumber() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Documents (*.pdf)", "*.pdf"));

        File targetFile = fileChooser.showSaveDialog(billTable.getScene().getWindow());
        if (targetFile != null) {
            try {
                List<CallRecord> calls = callDAO.getCallsByCustomerAndPeriod(
                        bill.getCustomerId(), bill.getBillingMonth(), bill.getBillingYear()
                );
                pdfService.generateInvoicePdf(bill, calls, targetFile);
                AlertUtil.showInfo(billTable.getScene().getWindow(), "PDF Exported",
                        "Invoice PDF successfully exported to:\n" + targetFile.getAbsolutePath());
            } catch (Exception e) {
                AlertUtil.showError(billTable.getScene().getWindow(), "Export Failed", "Error exporting PDF: " + e.getMessage());
            }
        }
    }

    private void openInvoicePreview(Bill bill) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/invoice_preview.fxml"));
            Parent root = loader.load();

            InvoicePreviewController controller = loader.getController();
            List<CallRecord> calls = callDAO.getCallsByCustomerAndPeriod(
                    bill.getCustomerId(), bill.getBillingMonth(), bill.getBillingYear()
            );
            controller.initData(bill, calls);

            Stage previewStage = new Stage();
            previewStage.setTitle("Invoice Preview - " + bill.getBillNumber());
            previewStage.initModality(Modality.APPLICATION_MODAL);
            previewStage.initOwner(billTable.getScene().getWindow());
            Scene scene = new Scene(root, 880, 720);
            previewStage.setScene(scene);
            previewStage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to open invoice preview modal", e);
            AlertUtil.showError(billTable.getScene().getWindow(), "Error", "Could not render invoice preview.");
        }
    }

    @FXML
    private void handleExportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Bills to CSV");
        fileChooser.setInitialFileName("telecom_bills_export.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));

        File file = fileChooser.showSaveDialog(billTable.getScene().getWindow());
        if (file != null) {
            try {
                csvService.exportBillsToCsv(filteredBillsList, file);
                AlertUtil.showInfo(billTable.getScene().getWindow(), "CSV Exported", "Billing report exported successfully!");
            } catch (IOException e) {
                AlertUtil.showError(billTable.getScene().getWindow(), "Export Failed", e.getMessage());
            }
        }
    }

    private double parseDoubleSafe(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
