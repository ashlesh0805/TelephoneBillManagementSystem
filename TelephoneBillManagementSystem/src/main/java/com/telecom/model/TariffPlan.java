package com.telecom.model;

/**
 * Entity representing a Tariff Plan with billing rates and allowances.
 */
public class TariffPlan {
    private int id;
    private String name;
    private double ratePerMinLocal;
    private double ratePerMinStd;
    private double ratePerMinIsd;
    private double monthlyRental;
    private int freeMinutes;
    private double smsRate;
    private double dataRate;
    private String description;

    public TariffPlan() {
    }

    public TariffPlan(int id, String name, double ratePerMinLocal, double ratePerMinStd, double ratePerMinIsd,
                      double monthlyRental, int freeMinutes, double smsRate, double dataRate, String description) {
        this.id = id;
        this.name = name;
        this.ratePerMinLocal = ratePerMinLocal;
        this.ratePerMinStd = ratePerMinStd;
        this.ratePerMinIsd = ratePerMinIsd;
        this.monthlyRental = monthlyRental;
        this.freeMinutes = freeMinutes;
        this.smsRate = smsRate;
        this.dataRate = dataRate;
        this.description = description;
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

    public double getRatePerMinLocal() {
        return ratePerMinLocal;
    }

    public void setRatePerMinLocal(double ratePerMinLocal) {
        this.ratePerMinLocal = ratePerMinLocal;
    }

    public double getRatePerMinStd() {
        return ratePerMinStd;
    }

    public void setRatePerMinStd(double ratePerMinStd) {
        this.ratePerMinStd = ratePerMinStd;
    }

    public double getRatePerMinIsd() {
        return ratePerMinIsd;
    }

    public void setRatePerMinIsd(double ratePerMinIsd) {
        this.ratePerMinIsd = ratePerMinIsd;
    }

    public double getMonthlyRental() {
        return monthlyRental;
    }

    public void setMonthlyRental(double monthlyRental) {
        this.monthlyRental = monthlyRental;
    }

    public int getFreeMinutes() {
        return freeMinutes;
    }

    public void setFreeMinutes(int freeMinutes) {
        this.freeMinutes = freeMinutes;
    }

    public double getSmsRate() {
        return smsRate;
    }

    public void setSmsRate(double smsRate) {
        this.smsRate = smsRate;
    }

    public double getDataRate() {
        return dataRate;
    }

    public void setDataRate(double dataRate) {
        this.dataRate = dataRate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRateForCallType(CallType type) {
        return switch (type) {
            case LOCAL -> ratePerMinLocal;
            case STD -> ratePerMinStd;
            case ISD -> ratePerMinIsd;
        };
    }

    @Override
    public String toString() {
        return name + " (₹" + String.format("%.2f", monthlyRental) + "/mo)";
    }
}
