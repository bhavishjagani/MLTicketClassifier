package com.SwiftSort.MLTicketClassifier.dto;

public class AnalyticsOverview {
    private double avgResolutionHours;
    private double reopenRate;
    private double negativePercent;
    private int slaBreaches;

    public AnalyticsOverview() {}

    public AnalyticsOverview(double avgResolutionHours, double reopenRate, double negativePercent, int slaBreaches) {
        this.avgResolutionHours = avgResolutionHours;
        this.reopenRate = reopenRate;
        this.negativePercent = negativePercent;
        this.slaBreaches = slaBreaches;
    }

    public double getAvgResolutionHours() { return avgResolutionHours; }
    public void setAvgResolutionHours(double avgResolutionHours) { this.avgResolutionHours = avgResolutionHours; }

    public double getReopenRate() { return reopenRate; }
    public void setReopenRate(double reopenRate) { this.reopenRate = reopenRate; }

    public double getNegativePercent() { return negativePercent; }
    public void setNegativePercent(double negativePercent) { this.negativePercent = negativePercent; }

    public int getSlaBreaches() { return slaBreaches; }
    public void setSlaBreaches(int slaBreaches) { this.slaBreaches = slaBreaches; }
}