package com.telecom.model;

/**
 * Entity representing a payment transaction against an invoice.
 */
public class Payment {
    private int id;
    private int billId;
    private String billNumber;      // Joined
    private String customerName;    // Joined
    private double amountPaid;
    private String paymentDate;
    private String paymentMode;     // 'CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'UPI', 'NET_BANKING'
    private String transactionReference;
    private String notes;

    public Payment() {
    }

    public Payment(int id, int billId, String billNumber, String customerName, double amountPaid,
                   String paymentDate, String paymentMode, String transactionReference, String notes) {
        this.id = id;
        this.billId = billId;
        this.billNumber = billNumber;
        this.customerName = customerName;
        this.amountPaid = amountPaid;
        this.paymentDate = paymentDate;
        this.paymentMode = paymentMode;
        this.transactionReference = transactionReference;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
