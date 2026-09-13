package com.telecom.model;

/**
 * Entity representing an individual Call Detail Record (CDR).
 */
public class CallRecord {
    private int id;
    private int customerId;
    private String customerName; // Joined
    private String customerPhone; // Joined
    private String callTimestamp;
    private String destinationNumber;
    private CallType callType;
    private int durationSeconds;
    private double computedCost;

    public CallRecord() {
    }

    public CallRecord(int id, int customerId, String customerName, String customerPhone,
                      String callTimestamp, String destinationNumber, CallType callType,
                      int durationSeconds, double computedCost) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.callTimestamp = callTimestamp;
        this.destinationNumber = destinationNumber;
        this.callType = callType;
        this.durationSeconds = durationSeconds;
        this.computedCost = computedCost;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getCallTimestamp() {
        return callTimestamp;
    }

    public void setCallTimestamp(String callTimestamp) {
        this.callTimestamp = callTimestamp;
    }

    public String getDestinationNumber() {
        return destinationNumber;
    }

    public void setDestinationNumber(String destinationNumber) {
        this.destinationNumber = destinationNumber;
    }

    public CallType getCallType() {
        return callType;
    }

    public void setCallType(CallType callType) {
        this.callType = callType;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public double getComputedCost() {
        return computedCost;
    }

    public void setComputedCost(double computedCost) {
        this.computedCost = computedCost;
    }

    public String getFormattedDuration() {
        int mins = durationSeconds / 60;
        int secs = durationSeconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    public double getDurationMinutes() {
        return Math.ceil(durationSeconds / 60.0);
    }
}
