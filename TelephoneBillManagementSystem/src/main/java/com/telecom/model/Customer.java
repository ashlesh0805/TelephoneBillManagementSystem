package com.telecom.model;

/**
 * Entity representing a telecom subscriber/customer.
 */
public class Customer {
    private int id;
    private String name;
    private String phoneNumber;
    private String email;
    private String address;
    private String connectionDate;
    private int planId;
    private String planName; // Joined from tariff_plans
    private String status;   // 'ACTIVE', 'INACTIVE', 'SUSPENDED'

    public Customer() {
    }

    public Customer(int id, String name, String phoneNumber, String email, String address,
                    String connectionDate, int planId, String planName, String status) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.connectionDate = connectionDate;
        this.planId = planId;
        this.planName = planName;
        this.status = status != null ? status : "ACTIVE";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getConnectionDate() {
        return connectionDate;
    }

    public void setConnectionDate(String connectionDate) {
        this.connectionDate = connectionDate;
    }

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    @Override
    public String toString() {
        return name + " (" + phoneNumber + ")";
    }
}
