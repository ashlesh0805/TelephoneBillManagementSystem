package com.telecom.model;

/**
 * Entity representing an itemized telephone bill generated for a customer.
 */
public class Bill {
    private int id;
    private String billNumber;
    private int customerId;
    private String customerName;    // Joined
    private String customerPhone;   // Joined
    private String customerAddress; // Joined
    private String planName;        // Joined
    private int billingMonth;
    private int billingYear;
    private double totalCallCharges;
    private double rentalCharges;
    private double taxPercentage;
    private double taxAmount;
    private double discountAmount;
    private double lateFee;
    private double totalAmount;
    private String dueDate;
    private String paymentStatus;   // 'PAID', 'PARTIALLY_PAID', 'UNPAID', 'OVERDUE'
    private String generatedDate;

    public Bill() {
    }

    public Bill(int id, String billNumber, int customerId, String customerName, String customerPhone,
                String customerAddress, String planName, int billingMonth, int billingYear,
                double totalCallCharges, double rentalCharges, double taxPercentage, double taxAmount,
                double discountAmount, double lateFee, double totalAmount, String dueDate,
                String paymentStatus, String generatedDate) {
        this.id = id;
        this.billNumber = billNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerAddress = customerAddress;
        this.planName = planName;
        this.billingMonth = billingMonth;
        this.billingYear = billingYear;
        this.totalCallCharges = totalCallCharges;
        this.rentalCharges = rentalCharges;
        this.taxPercentage = taxPercentage;
        this.taxAmount = taxAmount;
        this.discountAmount = discountAmount;
        this.lateFee = lateFee;
        this.totalAmount = totalAmount;
        this.dueDate = dueDate;
        this.paymentStatus = paymentStatus != null ? paymentStatus : "UNPAID";
        this.generatedDate = generatedDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public int getBillingMonth() {
        return billingMonth;
    }

    public void setBillingMonth(int billingMonth) {
        this.billingMonth = billingMonth;
    }

    public int getBillingYear() {
        return billingYear;
    }

    public void setBillingYear(int billingYear) {
        this.billingYear = billingYear;
    }

    public double getTotalCallCharges() {
        return totalCallCharges;
    }

    public void setTotalCallCharges(double totalCallCharges) {
        this.totalCallCharges = totalCallCharges;
    }

    public double getRentalCharges() {
        return rentalCharges;
    }

    public void setRentalCharges(double rentalCharges) {
        this.rentalCharges = rentalCharges;
    }

    public double getTaxPercentage() {
        return taxPercentage;
    }

    public void setTaxPercentage(double taxPercentage) {
        this.taxPercentage = taxPercentage;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getLateFee() {
        return lateFee;
    }

    public void setLateFee(double lateFee) {
        this.lateFee = lateFee;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(String generatedDate) {
        this.generatedDate = generatedDate;
    }

    public String getBillingPeriod() {
        return String.format("%02d/%d", billingMonth, billingYear);
    }

    public double getSubtotal() {
        return totalCallCharges + rentalCharges;
    }
}
